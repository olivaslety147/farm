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
package com.zerocracy.bundles.elects_and_assigns_performer

import com.jcabi.xml.XML
import com.mongodb.client.model.Filters
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.claims.Footprint
import com.zerocracy.pm.in.Orders
import org.cactoos.iterable.LengthOf
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual

def exec(Project project, XML xml) {
  String job = 'gh:test/test#1'
  Orders orders = new Orders(farm, project).bootstrap()
  MatcherAssert.assertThat(
    'Performer wasn\'t assigned to the job',
    orders.performer(job),
    new IsEqual<>('yegor256')
  )
  Farm farm = binding.variables.farm
  MatcherAssert.assertThat(
    '"Performer was elected" claim was not found',
    new LengthOf(
      new Footprint(farm, project).collection().find(
        Filters.and(
          Filters.eq('project', project.pid()),
          Filters.eq('type', 'Performer was elected'),
          Filters.eq('job', job)
        )
      )
    ).intValue(),
    new IsEqual<>(1)
  )
}
