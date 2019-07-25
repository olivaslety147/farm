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
package com.zerocracy.bundles.pr_assigns_performer

import com.jcabi.xml.XML
import com.zerocracy.Project
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.scope.Wbs

def exec(Project project, XML xml) {
  // @todo #1892:30min This test is failing on rultor but is working fine
  //  on local and on travis CI. It seems there is some issue with parallel
  //  test execution (may be I'm wrong about it, just a guess).
//  def job = 'gh:test/test#1'
//  assert new Orders(farm, project).performer(job) == 'krzyk'
//  assert new Wbs(project).role(job) == 'REV'
//  assert new Boosts(farm, project).factor(job) == 1
}
