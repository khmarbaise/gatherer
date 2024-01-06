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

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Gatherer;

class IntegratorTest {

  private static <T> Gatherer<T, ?, T> mapNoOp() {
    Gatherer.Integrator<Void, T, T> integrator = (_, element, downstream) -> {
      downstream.push(element);
      return true;
    };
    return Gatherer.ofSequential(integrator);
  }

  // <A, T, R>
  // A state, T element, Downstream<? super R> downstream

  private static final Gatherer.Integrator<Void, Integer, ? super Integer> noOp =
      //We could use "_" instead of "state"!
      (state, element, downstream) -> {
        downstream.push(element);
        return true;
      };


  @Test
  void noOperation_withGathererOf() {
    var integerList = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    var resultList = integerList.stream()
        .gather(Gatherer.of(noOp))
        .toList();
    System.out.println("resultList = " + resultList);
  }

  @Test
  void noOperationMapping() {
    var integerList = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    var resultList = integerList.stream()
        .map(Function.identity())
        .toList();
    System.out.println("resultList = " + resultList);
  }

  @Test
  void noOperation_Integration() {
    var integerList = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    var resultList = integerList.stream()
        .gather(mapNoOp())
        .toList();
    System.out.println("resultList = " + resultList);
  }

  @Test
  void noOperation_IntegrationDifferentType() {
    var integerList = List.of("1", "2", "3", "4", "5", "6", "7", "8");

    var resultList = integerList.stream()
        .gather(mapNoOp())
        .toList();
    System.out.println("resultList = " + resultList);
  }

  private static <T> Gatherer<T, Void, T> no_operation() {
    Gatherer.Integrator<Void, T, T> integrator = (_, element, downstream) -> {
      downstream.push(element);
      return true;
    };
    return Gatherer.of(integrator);
  }

  @Test
  void another_no_operation() {
    var integerList = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    var resultList = integerList.stream()
        .gather(no_operation())
        .toList();
    System.out.println("resultList = " + resultList);
  }
}
