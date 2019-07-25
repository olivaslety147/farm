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
package com.zerocracy.farm.ruled;

import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Scalar;

/**
 * XML file area, e.g. "pm/scope/wbs".

 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RdArea implements Scalar<String> {

    /**
     * The file.
     */
    private final Path path;

    /**
     * Ctor.
     * @param file The XML file
     */
    RdArea(final Path file) {
        this.path = file;
    }

    @Override
    public String value() throws IOException {
        return StringUtils.substringBeforeLast(
            StringUtils.substringAfter(
                new XMLDocument(
                    this.path.toFile()
                ).xpath("/*/@xsi:noNamespaceSchemaLocation").get(0),
                "/xsd/"
            ),
            ".xsd"
        );
    }

}
