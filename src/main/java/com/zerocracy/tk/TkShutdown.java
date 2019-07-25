/*
 * Copyright (c) 2016-2019 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHeaders;
import org.takes.rs.RsEmpty;

/**
 * Shutdown the app.
 *
 * @since 1.0
 */
public final class TkShutdown implements Take {
    /**
     * Properties.
     */
    private final Props props;
    /**
     * Farm.
     */
    private final Farm frm;
    /**
     * Ctor.
     * @param properties Properties.
     * @param farm Farm
     */
    public TkShutdown(final Props properties, final Farm farm) {
        this.props = properties;
        this.frm = farm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String hdr = new RqHeaders.Smart(new RqHeaders.Base(req))
            .single("X-Auth", "");
        if (!hdr.equals(this.props.get("//shutdown/header", "test"))) {
            throw new RsForward(
                new RsParFlash(
                    "You are not allowed to shutdown",
                    Level.WARNING
                )
            );
        }
        this.frm.close();
        return new RsEmpty();
    }
}
