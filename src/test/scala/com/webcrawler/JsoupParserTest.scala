package com.webcrawler

import java.io.File
import java.nio.charset.StandardCharsets

import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._

class JsoupParserTest extends munit.CatsEffectSuite {

  test("test getLinks parser") {
    val links = Jsoup
      .parse(
        new File("src/test/resources/test.html"),
        StandardCharsets.UTF_8.toString,
        "https://monzo.com/"
      )
      .select("a[href]")
      .asScala
      .toList
      .map(_.attr("abs:href"))
      .filter(_.contains("https://monzo.com/"))
      .map(Webpage)
    assertEquals(links, expectedLinks)
  }

  val expectedLinks: List[Webpage] = List(
    Webpage(
      value = "https://monzo.com/#"
    ),
    Webpage(
      value = "https://monzo.com/"
    ),
    Webpage(
      value = "https://monzo.com/"
    ),
    Webpage(
      value = "https://monzo.com/"
    ),
    Webpage(
      value = "https://monzo.com/i/business"
    ),
    Webpage(
      value = "https://monzo.com/i/current-account/"
    ),
    Webpage(
      value = "https://monzo.com/i/monzo-plus/"
    ),
    Webpage(
      value = "https://monzo.com/i/monzo-premium/"
    ),
    Webpage(
      value = "https://monzo.com/i/business/"
    ),
    Webpage(
      value = "https://monzo.com/features/joint-accounts/"
    ),
    Webpage(
      value = "https://monzo.com/features/16-plus/"
    ),
    Webpage(
      value = "https://monzo.com/i/savingwithmonzo/"
    ),
    Webpage(
      value = "https://monzo.com/features/savings/"
    ),
    Webpage(
      value = "https://monzo.com/isa"
    ),
    Webpage(
      value = "https://monzo.com/i/overdrafts/"
    ),
    Webpage(
      value = "https://monzo.com/i/loans/"
    ),
    Webpage(
      value = "https://monzo.com/blog/2019/11/12/what-are-unsecured-loans/"
    ),
    Webpage(
      value = "https://monzo.com/features/travel/"
    ),
    Webpage(
      value = "https://monzo.com/features/energy-switching/"
    ),
    Webpage(
      value = "https://monzo.com/i/shared-tabs-more/"
    ),
    Webpage(
      value = "https://monzo.com/community/making-monzo/"
    ),
    Webpage(
      value = "https://monzo.com/help/"
    ),
    Webpage(
      value = "https://monzo.com/i/coronavirus-faq"
    ),
    Webpage(
      value = "https://monzo.com/i/monzo-premium/"
    ),
    Webpage(
      value = "https://monzo.com/i/monzo-plus/"
    )
  )
}
