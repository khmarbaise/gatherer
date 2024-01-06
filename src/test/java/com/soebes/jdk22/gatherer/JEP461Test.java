/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.soebes.jdk22.gatherer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

class JEP461Test {

  record DistinctByLength(String str) {
    @Override
    public boolean equals(Object obj) {
      return obj instanceof DistinctByLength(String other)
             && str.length() == other.length();
    }

    @Override
    public int hashCode() {
      return str == null ? 0 : Integer.hashCode(str.length());
    }
  }

  @Test
  void example_one() {
    var result = Stream.of("123456", "foo", "bar", "baz", "quux", "anton", "egon", "banton")
        .map(DistinctByLength::new)
        .distinct()
        .map(DistinctByLength::str)
        .toList();

    System.out.println("result = " + result);
  }
}
