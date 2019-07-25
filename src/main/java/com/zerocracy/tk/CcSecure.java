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
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.codecs.CcAes;
import org.takes.facets.auth.codecs.CcCompact;
import org.takes.facets.auth.codecs.CcHex;
import org.takes.facets.auth.codecs.CcSafe;
import org.takes.facets.auth.codecs.Codec;

/**
 * Our secure codec.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class CcSecure implements Codec {
    /**
     * Original codec.
     */
    private final Codec origin;

    /**
     * Ctor.
     * @param farm The farm
     * @throws IOException If fails
     */
    CcSecure(final Farm farm) throws IOException {
        this.origin = new CcSafe(
            new CcHex(
                new CcAes(
                    new CcCompact(),
                    new Props(farm).get(
                        "//security/aes.key",
                        "0123456701234567"
                    )
                )
            )
        );
    }

    @Override
    public byte[] encode(final Identity identity) throws IOException {
        return this.origin.encode(identity);
    }

    @Override
    public Identity decode(final byte[] bytes) throws IOException {
        return this.origin.decode(bytes);
    }

}
