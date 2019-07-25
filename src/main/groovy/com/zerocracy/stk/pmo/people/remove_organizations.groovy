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
package com.zerocracy.stk.pmo.people

import com.jcabi.github.User
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.claims.ClaimIn
import com.zerocracy.claims.ClaimOut
import com.zerocracy.entry.ClaimsOf
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.farm.props.Props
import com.zerocracy.pmo.People
import java.util.concurrent.TimeUnit

def exec(Project pmo, XML xml) {
  new Assume(pmo, xml).isPmo()
  new Assume(pmo, xml).type('Ping daily')
  Farm farm = binding.variables.farm
  People people = new People(farm).bootstrap()
  ClaimIn claim = new ClaimIn(xml)
  people.iterate().each { uid ->
    User.Smart user = new User.Smart(new ExtGithub(farm).value().users().get(uid))
    if (user.type() == 'Organization') {
      claim.copy()
        .type('Notify user')
        .token("user;$uid")
        .param(
          'message',
          new Par(
            'Your Github account: %s is an organization. ',
            'We do not support organization logins; ',
            'your profile will be deleted in %d hours.'
          ).say(uid, 24)
      ).postTo(new ClaimsOf(farm))
      ClaimOut delete = claim.copy()
        .type('Delete user')
        .param('login', uid)
      if (!new Props(farm).has('//testing')) {
        delete.until(TimeUnit.HOURS.toSeconds(24))
      }
      delete.postTo(new ClaimsOf(farm))
    }
  }
}
