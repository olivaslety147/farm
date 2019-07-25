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
package com.zerocracy.entry;

import com.zerocracy.Farm;
import com.zerocracy.radars.slack.SkSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * Slack sessions.
 *
 * @since 1.0
 */
public final class ExtSlack implements Scalar<Map<String, SkSession>> {

    /**
     * The singleton.
     */
    private static final
        UncheckedFunc<Farm, Map<String, SkSession>> SINGLETON =
        new UncheckedFunc<>(
            new SolidFunc<>(
                frm -> new ConcurrentHashMap<>(0)
            )
        );

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtSlack(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Map<String, SkSession> value() {
        return ExtSlack.SINGLETON.apply(this.farm);
    }

}
