### Scala Webcrawler
This webcrawler is built using the newly released Cats Effect 3.0, fs2.Streams and JSoup.

### Approach
The webcralwer is built to crawl a webpage and all pages found on that page on the same subdomain . This crawler is built to crawl ```n``` layers deep however the implementation provided allows a ```requiredDepth``` to be specified.

This is a breadth-first search problem with the implementation separating the searching and processing of pages. The crawler (engine) scapes pages and adds all links of the same subdomain to a queue. The engine then places a copy of the crawled link in a store so that it is not duplicated. The engine will then pull links from the queue and repeat. 

The provided domain will be crawled with the discovered links being crawled concurrently up to a limit of 2 streams. This can be updated within the ```IOEngine``` implementation. The implementation could be updated to enable this an input parameter for ```init``` so tuning could be done.

Error handling has also been kept to a minimum as this can be implemented at a later date. For example, retry logic for timeouts could be added up to a certain number of retries.

Test cases are used to ensure that the queue and repo function as required. No performance style testing has been put in place.
