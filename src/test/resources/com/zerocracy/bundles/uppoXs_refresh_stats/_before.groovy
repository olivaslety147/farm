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
package com.zerocracy.bundles.refresh_speed

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.farm.fake.FkProject
import com.zerocracy.pmo.Awards
import com.zerocracy.pmo.Speed

import java.time.Duration
import java.time.Instant

def exec(Project pmo, XML xml) {
  String login = 'developer'
  Project pkt = new FkProject()
  new Awards(pmo, login).bootstrap().with {
    add(pkt, 15, 'gh:test/test#1', 'test', new Date(1517432400000L))
    add(pkt, 100, 'gh:test/test#1', 'test', new Date(1525122000000L))
    add(pkt, 10, 'gh:test/test#1', 'test', new Date(1517432400001L))
  }
  new Speed(pmo, login).bootstrap().with {
    add(
      pkt.pid(), 'gh:test/speed#1', 10L,
      Instant.parse('2018-07-28T18:00:00.000Z') - Duration.ofDays(91)
    )
  }
}
