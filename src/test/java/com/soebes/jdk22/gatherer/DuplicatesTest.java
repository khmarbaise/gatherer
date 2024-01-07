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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicatesTest {

  @Test
  void exampleFindDuplicates() {
    var integers = List.of(100, 1, 10, 11, 5, 10, 11, 5, 100, 75, 78, 90);
    var duplicates = findDuplicates(integers);
    System.out.println("duplicates = " + duplicates);
  }

  // Classical implementation. Could be done better ;-)
  List<Integer> findDuplicates(List<Integer> givenList) {
    long count = givenList.stream().distinct().count();
    if (count < givenList.size()) {
      return givenList.stream().filter(i -> Collections.frequency(givenList, i) > 1)
          .distinct().toList();
    } else {
      return List.of();
    }
  }

  @Test
  void exampleFindDuplicatesWithoutCombiner() {
    var integers = List.of(100, 1, 10, 11, 5, 10, 11, 5, 100, 75, 78, 90);
    var resultList = integers.stream().gather(duplicatesWithoutCombiner()).toList();

    assertThat(resultList).containsExactlyInAnyOrder(100, 10, 11, 5);
    System.out.println("resultList = " + resultList);
  }

  @Test
  void exampleFindDuplicatesWithCombiner() {
    var integers = List.of(100, 1, 10, 11, 5, 10, 11, 5, 100, 75, 78, 90);
    var resultList = integers.parallelStream().gather(duplicates()).toList();

    assertThat(resultList).containsExactlyInAnyOrder(100, 10, 11, 5);
  }

  @Test
  void exampleFindDuplicatesWithGathererCombinerForStrings() {
    var integers = List.of("A", "BB", "A", "C", "BB", "DD", "EE", "F");
    var resultList = integers.parallelStream().gather(duplicates()).toList();

    assertThat(resultList).containsExactlyInAnyOrder("A", "BB");
  }

  /**
   * This {@link Gatherer} will result in all duplicates within the {@link java.util.stream.Stream}.
   * @param <T>
   * @return {@link Gatherer}
   */
  static <T> Gatherer<? super T, ?, T> duplicates() {
    Supplier<HashMap<T, Integer>> initializer = HashMap::new;
    //
    Gatherer.Integrator<HashMap<T, Integer>, T, T> integrator = (state, element, _) -> {
      var currentNumber = state.getOrDefault(element, 0);
      state.put(element, currentNumber + 1);
      return true;
    };
    //
    BiConsumer<HashMap<T, Integer>, Gatherer.Downstream<? super T>> finisher = (state, downstream) -> {
      state.forEach((k, v) -> {
        if (v >= 2) {
          downstream.push(k);
        }
      });
    };
    //
    BinaryOperator<HashMap<T, Integer>> combiner = (s1, s2) -> {
      s1.forEach((k, v) -> {
        var s2def = s2.getOrDefault(k, 0);
        s2.put(k, v + s2def);
      });
      return s2;
    };
    //
    return Gatherer.of(initializer, integrator, combiner, finisher);
  }

  /**
   * An implementation without {@link Gatherer#combiner()}.
   *
   * @param <T>
   * @return
   */
  static <T> Gatherer<? super T, ?, T> duplicatesWithoutCombiner() {
    Supplier<HashMap<T, Integer>> initializer = HashMap::new;
    //
    Gatherer.Integrator<HashMap<T, Integer>, T, T> integrator = (state, element, _) -> {
      var currentNumber = state.getOrDefault(element, 0);
      state.put(element, currentNumber + 1);
      return true;
    };
    //
    BiConsumer<HashMap<T, Integer>, Gatherer.Downstream<? super T>> finisher = (state, downstream) -> {
      state.forEach((k, v) -> {
        if (v >= 2) {
          downstream.push(k);
        }
      });
    };
    //
    return Gatherer.ofSequential(initializer, integrator, finisher);
  }
}
