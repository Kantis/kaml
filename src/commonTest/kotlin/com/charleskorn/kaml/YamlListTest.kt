/*

   Copyright 2018-2021 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package com.charleskorn.kaml

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class YamlListTest : DescribeSpec({
    describe("a YAML list") {
        describe("testing equivalence") {
            val list = YamlList(
                listOf(
                    YamlScalar("item 1", YamlPath.root.withListEntry(0, Location(4, 5))),
                    YamlScalar("item 2", YamlPath.root.withListEntry(1, Location(6, 7)))
                ),
                YamlPath.root
            )

            describe("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    list.equivalentContentTo(list) shouldBe true
                }
            }

            describe("comparing it to another list with the same items in the same order with a different path") {
                it("indicates that they are equivalent") {
                    list.equivalentContentTo(YamlList(list.items, YamlPath.root.withMapElementValue(Location(5, 6)))) shouldBe true
                }
            }

            describe("comparing it to another list with the same items in a different order with the same path") {
                it("indicates that they are not equivalent") {
                    list.equivalentContentTo(YamlList(list.items.reversed(), list.path)) shouldBe false
                }
            }

            describe("comparing it to another list with different items with the same path") {
                it("indicates that they are not equivalent") {
                    list.equivalentContentTo(YamlList(emptyList(), list.path)) shouldBe false
                }
            }

            describe("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    list.equivalentContentTo(YamlScalar("some content", list.path)) shouldBe false
                }
            }

            describe("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    list.equivalentContentTo(YamlNull(list.path)) shouldBe false
                }
            }

            describe("comparing it to a map") {
                it("indicates that they are not equivalent") {
                    list.equivalentContentTo(YamlMap(emptyMap(), list.path)) shouldBe false
                }
            }
        }

        describe("getting elements from a list") {
            val firstItemPath = YamlPath.root.withListEntry(0, Location(4, 5))
            val secondItemPath = YamlPath.root.withListEntry(1, Location(6, 7))

            val list = YamlList(
                listOf(
                    YamlScalar("item 1", firstItemPath),
                    YamlScalar("item 2", secondItemPath)
                ),
                YamlPath.root
            )

            describe("getting element in bounds") {
                it("returns element") {
                    list[0] shouldBe YamlScalar("item 1", firstItemPath)
                    list[1] shouldBe YamlScalar("item 2", secondItemPath)
                }
            }

            describe("getting element out of bounds") {
                it("throws IndexOutOfBoundsException") {
                    shouldThrow<IndexOutOfBoundsException> { list[2] }
                    shouldThrow<IndexOutOfBoundsException> { list[10] }
                }
            }
        }

        describe("converting the content to a human-readable string") {
            context("an empty list") {
                val list = YamlList(emptyList(), YamlPath.root)

                it("returns empty square brackets") {
                    list.contentToString() shouldBe "[]"
                }
            }

            context("a list with a single entry") {
                val list = YamlList(listOf(YamlScalar("hello", YamlPath.root.withListEntry(0, Location(1, 1)))), YamlPath.root)

                it("returns that item surrounded by square brackets") {
                    list.contentToString() shouldBe "['hello']"
                }
            }

            context("a list with multiple entries") {
                val list = YamlList(
                    listOf(
                        YamlScalar("hello", YamlPath.root.withListEntry(0, Location(1, 1))),
                        YamlScalar("world", YamlPath.root.withListEntry(1, Location(2, 1)))
                    ),
                    YamlPath.root
                )

                it("returns all items separated by commas and surrounded by square brackets") {
                    list.contentToString() shouldBe "['hello', 'world']"
                }
            }
        }

        describe("replacing its path") {
            val original = YamlList(
                listOf(
                    YamlScalar("hello", YamlPath.root.withListEntry(0, Location(1, 1))),
                    YamlScalar("world", YamlPath.root.withListEntry(1, Location(2, 1)))
                ),
                YamlPath.root
            )

            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))

            val expected = YamlList(
                listOf(
                    YamlScalar("hello", newPath.withListEntry(0, Location(1, 1))),
                    YamlScalar("world", newPath.withListEntry(1, Location(2, 1)))
                ),
                newPath
            )

            it("returns a new list node with the path for the node and its items updated to the new path") {
                original.withPath(newPath) shouldBe expected
            }
        }

        describe("converting it to a string") {
            val path = YamlPath.root.withMapElementKey("test", Location(2, 1)).withMapElementValue(Location(2, 7))
            val elementPath = path.withListEntry(0, Location(3, 3))
            val value = YamlList(listOf(YamlScalar("hello", elementPath)), path)

            it("returns a human-readable description of itself") {
                value.toString() shouldBe
                    """
                        list @ $path (size: 1)
                        - item 0:
                          scalar @ $elementPath : hello
                    """.trimIndent()
            }
        }
    }
})
