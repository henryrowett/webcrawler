package com.webcrawler

import java.io.File
import java.nio.charset.StandardCharsets

import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._

class EngineTest extends munit.CatsEffectSuite {

  test("test getLinks parser") {
    val links = Jsoup.parse(
        new File("src/test/resources/test_html.html"),
        StandardCharsets.UTF_8.toString,
        "https://monzo.com/")
      .select("a[href]")
      .asScala
      .toList
      .map { e =>
        val href = e.attr("href")
        if (!(href.contains("http") || href.contains("https"))) {
          Webpage(e.baseUri().concat(href.substring(1)))
        }
        else Webpage(href)
      }
    assertEquals(links, expectedLinks)
  }

  val expectedLinks = List(
    Webpage(
    url = "https://monzo.com/"
    ),
    Webpage(
      url = "https://monzo.com/"
    ),
    Webpage(
      url = "https://monzo.com/"
    ),
    Webpage(
      url = "https://monzo.com/"
    ),
    Webpage(
      url = "https://monzo.com/i/business"
    ),
    Webpage(
      url = "https://monzo.com/i/current-account/"
    ),
    Webpage(
      url = "https://monzo.com/i/monzo-plus/"
    ),
    Webpage(
      url = "https://monzo.com/i/monzo-premium/"
    ),
    Webpage(
      url = "https://monzo.com/i/business/"
    ),
    Webpage(
      url = "https://monzo.com/features/joint-accounts/"
    ),
    Webpage(
      url = "https://monzo.com/features/16-plus/"
    ),
    Webpage(
      url = "https://monzo.com/i/savingwithmonzo/"
    ),
    Webpage(
      url = "https://monzo.com/features/savings/"
    ),
    Webpage(
      url = "https://monzo.com/isa"
    ),
    Webpage(
      url = "https://monzo.com/i/overdrafts/"
    ),
    Webpage(
      url = "https://monzo.com/i/loans/"
    ),
    Webpage(
      url = "https://monzo.com/blog/2019/11/12/what-are-unsecured-loans/"
    ),
    Webpage(
      url = "https://monzo.com/features/travel/"
    ),
    Webpage(
      url = "https://monzo.com/features/energy-switching/"
    ),
    Webpage(
      url = "https://monzo.com/i/shared-tabs-more/"
    ),
    Webpage(
      url = "https://monzo.com/community/making-monzo/"
    ),
    Webpage(
      url = "https://monzo.com/help/"
    ),
    Webpage(
      url = "https://app.adjust.com/ydi27sn_9mq4ox7?engagement_type=fallback_click&fallback=https%3A%2F%2Fmonzo.com%2Fdownload&redirect_macos=https%3A%2F%2Fmonzo.com%2Fdownload"
    ),
    Webpage(
      url = "https://monzo.com/i/coronavirus-faq"
    ),
    Webpage(
      url = "https://monzo.com/i/monzo-premium/"
    ),
    Webpage(
      url = "https://monzo.com/i/monzo-plus/"
    )
  )
}