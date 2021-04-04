### Scala Webcrawler
This webcrawler is built using the newly released Cats Effect 3.0, with help from http4s, fs2.Streams and a number of other related libraries.

### Approach
The webcralwer is built to crawl a webpage and all subdomains found on that page. This crawler is built to crawl ```n``` layers deep however the implementation provided only goes one layer deep on the original domain. This can be updated.

All links found on the original page will be printed to the console. All links that are crawlable will be crawled and so on. 

This is a breadth-first search problem with the algorithm separating the searching and processing of pages. The searcher/scraper will scrape pages and add all links to the queue then place a copy of the link in the store so that it is not duplicated. The processor will pull a link and log all corresponding links. 

The provided domain will be crawled with the discovered links being crawled concurrently up to a limit of 10 pages being crawled concurrently. This is an implementation detail and can be edited as required based on the requirements of the machine.
