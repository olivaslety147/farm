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
package com.zerocracy.stk.pm.in.impediments

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.SoftException
import com.zerocracy.claims.ClaimIn
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.farm.Assume
import com.zerocracy.pm.in.Impediments
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo().type('Register impediment')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String author = claim.author()
  String reason = new Par('@%s asked to wait a bit').say(author)
  Farm farm = binding.variables.farm
  boolean allowed = new Orders(farm, project).bootstrap().performer(job) == author ||
    new Roles(project).bootstrap().hasRole(author, 'PO', 'ARC')
  if (!allowed) {
    throw new SoftException(
      new Par(farm, '@%s you can\'t register impediment for this job').say(author)
    )
  }
  new Impediments(farm, project)
    .bootstrap()
    .register(job, reason)
  claim.reply(
    new Par(
      'The impediment for %s was registered successfully by @%s'
    ).say(job, author)
  ).postTo(new ClaimsOf(farm, project))
  claim.copy()
    .type('Impediment was registered')
    .param('job', job)
    .param('reason', reason)
    .postTo(new ClaimsOf(farm, project))
}
