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
package com.zerocracy.bundles.delete_stale_qa_jobs

import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Project
import com.zerocracy.cash.Cash
import com.zerocracy.pm.qa.Reviews
import com.zerocracy.pmo.Agenda
import com.zerocracy.pmo.Pmo
import com.zerocracy.pmo.Projects

def exec(Project project, XML xml) {
  Farm farm = binding.variables.farm
  new Projects(new Pmo(farm), 'test').bootstrap().add('TESTPROJECT')
  new Agenda(farm, 'test').bootstrap().add(farm.find("@id='TESTPROJECT'")[0], 'gh:test/test#1', 'QA')
  new Agenda(farm, 'test').bootstrap().add(farm.find("@id='TESTPROJECT'")[0], 'gh:test/test#2', 'QA')
  new Agenda(farm, 'test').bootstrap().add(farm.find("@id='TESTPROJECT'")[0], 'gh:test/test#3', 'DEV')
  new Reviews(
    farm.find("@id='TESTPROJECT'")[0]
  ).bootstrap().add('gh:test/test#2','test', 'g4s8', new Cash.S('$10'), 30, new Cash.S('$0'))
  new Reviews(
    farm.find("@id='TESTPROJECT'")[0]
  ).bootstrap().add('gh:test/test#3','g4s8', 'test', new Cash.S('$10'), 30, new  Cash.S('$0'))
}
