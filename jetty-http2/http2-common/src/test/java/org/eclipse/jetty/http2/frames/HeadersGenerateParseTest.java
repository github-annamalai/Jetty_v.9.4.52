//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.http2.frames;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.eclipse.jetty.http.HostPortHttpField;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.generator.HeaderGenerator;
import org.eclipse.jetty.http2.generator.HeadersGenerator;
import org.eclipse.jetty.http2.hpack.HpackEncoder;
import org.eclipse.jetty.http2.parser.Parser;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.MappedByteBufferPool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeadersGenerateParseTest
{
    private final ByteBufferPool byteBufferPool = new MappedByteBufferPool();

    @Test
    public void testGenerateParse() throws Exception
    {
        HeadersGenerator generator = new HeadersGenerator(new HeaderGenerator(), new HpackEncoder());

        int streamId = 13;
        HttpFields fields = new HttpFields();
        fields.put("Accept", "text/html");
        fields.put("User-Agent", "Jetty");
        MetaData.Request metaData = new MetaData.Request("GET", HttpScheme.HTTP, new HostPortHttpField("localhost:8080"), "/path", HttpVersion.HTTP_2, fields);

        final List<HeadersFrame> frames = new ArrayList<>();
        Parser parser = new Parser(byteBufferPool, new Parser.Listener.Adapter()
        {
            @Override
            public void onHeaders(HeadersFrame frame)
            {
                frames.add(frame);
            }
        }, 4096, 8192);
        parser.init(UnaryOperator.identity());

        // Iterate a few times to be sure generator and parser are properly reset.
        for (int i = 0; i < 2; ++i)
        {
            ByteBufferPool.Lease lease = new ByteBufferPool.Lease(byteBufferPool);
            PriorityFrame priorityFrame = new PriorityFrame(streamId, 3 * streamId, 200, true);
            generator.generateHeaders(lease, streamId, metaData, priorityFrame, true);

            frames.clear();
            for (ByteBuffer buffer : lease.getByteBuffers())
            {
                while (buffer.hasRemaining())
                {
                    parser.parse(buffer);
                }
            }

            assertEquals(1, frames.size());
            HeadersFrame frame = frames.get(0);
            assertEquals(streamId, frame.getStreamId());
            assertTrue(frame.isEndStream());
            MetaData.Request request = (MetaData.Request)frame.getMetaData();
            assertEquals(metaData.getMethod(), request.getMethod());
            assertEquals(metaData.getURI(), request.getURI());
            for (int j = 0; j < fields.size(); ++j)
            {
                HttpField field = fields.getField(j);
                assertTrue(request.getFields().contains(field));
            }
            PriorityFrame priority = frame.getPriority();
            assertNotNull(priority);
            assertEquals(priorityFrame.getStreamId(), priority.getStreamId());
            assertEquals(priorityFrame.getParentStreamId(), priority.getParentStreamId());
            assertEquals(priorityFrame.getWeight(), priority.getWeight());
            assertEquals(priorityFrame.isExclusive(), priority.isExclusive());
        }
    }

    @Test
    public void testGenerateParseOneByteAtATime() throws Exception
    {
        HeadersGenerator generator = new HeadersGenerator(new HeaderGenerator(), new HpackEncoder());

        final List<HeadersFrame> frames = new ArrayList<>();
        Parser parser = new Parser(byteBufferPool, new Parser.Listener.Adapter()
        {
            @Override
            public void onHeaders(HeadersFrame frame)
            {
                frames.add(frame);
            }
        }, 4096, 8192);
        parser.init(UnaryOperator.identity());

        // Iterate a few times to be sure generator and parser are properly reset.
        for (int i = 0; i < 2; ++i)
        {
            int streamId = 13;
            HttpFields fields = new HttpFields();
            fields.put("Accept", "text/html");
            fields.put("User-Agent", "Jetty");
            MetaData.Request metaData = new MetaData.Request("GET", HttpScheme.HTTP, new HostPortHttpField("localhost:8080"), "/path", HttpVersion.HTTP_2, fields);

            ByteBufferPool.Lease lease = new ByteBufferPool.Lease(byteBufferPool);
            PriorityFrame priorityFrame = new PriorityFrame(streamId, 3 * streamId, 200, true);
            generator.generateHeaders(lease, streamId, metaData, priorityFrame, true);

            frames.clear();
            for (ByteBuffer buffer : lease.getByteBuffers())
            {
                buffer = buffer.slice();
                while (buffer.hasRemaining())
                {
                    parser.parse(ByteBuffer.wrap(new byte[]{buffer.get()}));
                }
            }

            assertEquals(1, frames.size());
            HeadersFrame frame = frames.get(0);
            assertEquals(streamId, frame.getStreamId());
            assertTrue(frame.isEndStream());
            MetaData.Request request = (MetaData.Request)frame.getMetaData();
            assertEquals(metaData.getMethod(), request.getMethod());
            assertEquals(metaData.getURI(), request.getURI());
            for (int j = 0; j < fields.size(); ++j)
            {
                HttpField field = fields.getField(j);
                assertTrue(request.getFields().contains(field));
            }
            PriorityFrame priority = frame.getPriority();
            assertNotNull(priority);
            assertEquals(priorityFrame.getStreamId(), priority.getStreamId());
            assertEquals(priorityFrame.getParentStreamId(), priority.getParentStreamId());
            assertEquals(priorityFrame.getWeight(), priority.getWeight());
            assertEquals(priorityFrame.isExclusive(), priority.isExclusive());
        }
    }
}
