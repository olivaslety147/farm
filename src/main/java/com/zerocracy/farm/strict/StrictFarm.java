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
package com.zerocracy.farm.strict;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;

/**
 * Strict farm.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class StrictFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Ctor.
     * @param farm Original farm
     */
    public StrictFarm(final Farm farm) {
        this.origin = farm;
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return new Mapped<>(
            StrictProject::new,
            this.origin.find(query)
        );
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
