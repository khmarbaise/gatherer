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
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

class DistinctByTest {

  @Test
  void exampleDistinctWithNumbers() {
    var integers = List.of(1, 10, 11, 10, 11);
    var result = integers.stream().distinct().toList();
    System.out.println("result = " + result);
  }

  @Test
  void usingGroupingBy() {
    var result = Stream.of("123456", "foo", "bar", "baz", "quux", "anton", "egon", "banton")
        .collect(Collectors.groupingBy(String::length));
    System.out.println("result = " + result);
  }

  @Test
  void usingDistinctBy() {
    var stringStream = List.of("123456", "foo", "bar", "baz", "quux", "anton", "egon", "banton");
    var groupingBy = stringStream.stream().collect(Collectors.groupingBy(String::length));
    var result = stringStream
        .parallelStream()
        .gather(distinctBy(String::length))
        .toList();

    System.out.println("stringStream = " + stringStream);
    System.out.println("result = " + result);
    System.out.println("groupingBy = " + groupingBy);
  }

  @Test
  void usingDistrinctByExample() {
    var stringStream = List.of("123456", "foo", "bar", "baz", "quux", "anton", "egon", "banton");
    var result = stringStream
        .stream()
        .gather(distinctByWithoutFinisher(String::length))
        .toList();

    System.out.println("result = " + result);
  }

  @Test
  void usingDistrinctByStrange() {
    var stringStream = List.of(500, 200, 1, 2, 3, 4, 10, 20, 1, 50, 100, 50);
    var result = stringStream
        .stream()
        .gather(distinctByWithoutFinisher(Object::hashCode))
        .toList();

    System.out.println("result = " + result);
  }

  /**
   * The implementation without a {@link Gatherer#finisher()}
   * @param classifier The classifier to differentiate.
   * @param <T>
   * @param <A>
   * @return {@link Gatherer}
   */
  static <T, A> Gatherer<T, ?, T> distinctByWithoutFinisher(Function<? super T, ? extends A> classifier) {
    Supplier<HashMap<A, List<T>>> initializer = HashMap::new;
    //
    Gatherer.Integrator<HashMap<A, List<T>>, T, T> integrator = (state, element, downstream) -> {
      A apply = classifier.apply(element);
      state.computeIfAbsent(apply, (_) -> new ArrayList<>()).add(element);
      if (state.get(apply).size() == 1) {
        downstream.push(element);
      }
      return true;
    };
    //
    return Gatherer.ofSequential(initializer, integrator);
  }


  static <T, A> Gatherer<T, ?, T> distinctBy(Function<? super T, ? extends A> classifier) {
    Supplier<HashMap<A, List<T>>> initializer = HashMap::new;
    //
    Gatherer.Integrator<HashMap<A, List<T>>, T, T> integrator = (state, element, _) -> {
      A apply = classifier.apply(element);
      state.computeIfAbsent(apply, (_) -> new ArrayList<>()).add(element);

      return true;
    };
    //
    BiConsumer<HashMap<A, List<T>>, Gatherer.Downstream<? super T>> finisher = (state, downstream) -> {
      state.forEach((_, value) -> downstream.push(value.getLast()));
    };
    //
    return Gatherer.ofSequential(initializer, integrator, finisher);
  }
}
