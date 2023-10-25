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

package org.eclipse.jetty.websocket.api;

/**
 * A recoverable exception within the websocket framework.
 */
@SuppressWarnings("serial")
public class WebSocketException extends RuntimeException
{
    public WebSocketException()
    {
        super();
    }

    public WebSocketException(String message)
    {
        super(message);
    }

    public WebSocketException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WebSocketException(Throwable cause)
    {
        super(cause);
    }
}
