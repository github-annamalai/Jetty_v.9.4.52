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

package org.eclipse.jetty.websocket.common;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.component.LifeCycle;

public class SessionTracker extends AbstractLifeCycle implements WebSocketSessionListener, Dumpable
{
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Set<WebSocketSession> getSessions()
    {
        return Collections.unmodifiableSet(new HashSet<>(sessions));
    }

    @Override
    public void onSessionCreated(WebSocketSession session)
    {
        LifeCycle.start(session);
        sessions.add(session);
    }

    @Override
    public void onSessionClosed(WebSocketSession session)
    {
        sessions.remove(session);
        LifeCycle.stop(session);
    }

    @Override
    protected void doStop() throws Exception
    {
        for (WebSocketSession session : sessions)
        {
            LifeCycle.stop(session);
        }
        super.doStop();
    }

    @ManagedAttribute("Total number of active WebSocket Sessions")
    public int getNumSessions()
    {
        return sessions.size();
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException
    {
        Dumpable.dumpObjects(out, indent, this, sessions);
    }
}
