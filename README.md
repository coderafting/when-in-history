# WhenInHistory

[WhenInHistory](https://wheninhistory.herokuapp.com/) is a search engine for historical events. It presents search results in a form that humans can empathize with.

## Technical details

### The app
[WhenInHistory](https://wheninhistory.herokuapp.com/) app is written in [ClojureScript](https://clojurescript.org/). It uses [re-frame](https://github.com/Day8/re-frame) and its logic flow at its core.

### Search Experience
The current search experience is real-time (almost), as user types.

The search process starts when a user enters the 3rd character of the search string, and the process runs for each further change in the search string. It means that the search results are shown for each modified input, almost in real-time.

WhenInHistory uses [memsearch](https://github.com/coderafting/memsearch) library to provide a fast search experience.

### Human readable description of events
WhenInHistory uses [humane-time](https://github.com/coderafting/humane-time) library to produce descriptions of events.

The `humane-time` library expects date-time inputs to be in valid format. However, there are many events in history that do not have appropriate date-time formats, for example, earth was created about `4.54 billion years ago`. WhenInHistory handles such cases as well. Please refer to **`wheninhistory.utils/human-readable-timeline`**.

### Metrics measurement
Currently, I am primarily interested in search inputs and user-sessions. I want to understand:

- what searches are happening, overall and session-wise?
- how many sessions, parallel or otherwise?
- how many searches are happening per session?

**Session:**

The session for WhenInHistory is different from a web session, which is monitored via web cookies. A session starts when a user launches the WhenInHistory app in a browser tab, and ends when the user closes the browser tab.

**Cookies?** - No Cookies!

WhenInHistory doesn't put cookies in users' browsers. I am not in favor of putting cookies. There are better ways to achieve what a cookie can offer to WhenInHistory than compromising the privacy and browsing experience of a user.

So, I am using a logging service, [product-logger](https://product-logger.herokuapp.com/), that I have built. It is a server-side app, written in [Clojure](https://clojure.org/), with [ring](https://github.com/ring-clojure/ring) powering its core. I am yet to come up with a better name for it.

Each session has a unique id, and will be logged with this unique id and timestamp. The logic for logging search inputs are described below.

### Logging the search inputs
>NOTE: Logging process does not interfere with search experience (process).

Since the search experience is not based on `search-on-enter` or `search-on-button-click`, we will have a stream of incoming search inputs for each search. For a sample search, `world war`, the inputs will look like below:

```clojure
t=0: "w"
t=1: "wo"
t=2: "wor"
t=3: "worl"
t=4: "world"
t=5: "world "
t=6: "world w"
t=7: "world wa"
t=8: "world war"
```
It doesn't make sense to log each of these inputs. At the same time, we don't know if the user wanted to search `world` or `world war`.

#### PROBLEM 1

How do we identify search inputs that are appropriate to log? In other words, in the above example, how can we say that only `world war` should be logged, and rest should be discarded.

**Solution:** an in-memory mutable collection of valid (loggable) search inputs.

**Implementation details:**

For each session, we maintain an in-memory mutable collection of search inputs. The inputs contain a timestamp, along with the text. As the user enters the characters, the mutable collection gets updated in the following ways:

1. First, all the existing inputs in the collection are checked for their lifetime. If any input is **younger than 3 seconds**, they get removed.
2. Then, the latest input gets appended to the modified collection.

These two steps occur in the same computation process.

This way, the collection maintains inputs that are at least 3 seconds apart from one another with respect to their entries into the collection. So, in the above example, the collection might just have `world war` in it, because the difference between `t0` and `t8` might be less than 3 seconds.

**Why 3 seconds?**

The interval of 3 seconds can be modified. Currently, I have kept it at 3 seconds, based on my own search experience. The way to think about it is: **what could be the time difference between any two consecutive searches**. Such a time difference depends upon the **information and presentation** of search results. The information and presentation of WhenInHistory's search-results are different from that of Google's search-results. In sum, it is about how quickly a user can understand the presented results.

So, now we have achieved an in-memory mutable collection of valid (loggable) search inputs.

#### PROBLEM 2

How do we ensure the following:
- Logging each valid input in the collection only once, and
- Removing the logged inputs from the collection, to avoid the collection from growing large as a user's search volume increases in a single session.

**Solution:** a separate logging process that logs the valid inputs and removes them from the collection in the same computation process.

**Implementation details:**

The logging process works in the following fashion:

- The logger starts 3 seconds after the first input gets generated by a user, and runs for each character input (with 3 seconds delay).
- On each run, it consumes the input collection, looks for **inputs older than (or equal to) 3 seconds**, logs them, and finally removes them from the collection.

**IMPORTANT NOTES**

- The process that builds the in-memory input collection runs independently from the logging process.
- Both the processes are triggered by a user's search inputs, but run according to their own schedules.
- Both the processes consume and mutate the in-memory input collection, but at different times. The effects of mutations are different in the two processes.
- Both the processes are parts of the event handling mechanism of `re-frame`. Therefore, sequential handling of events and their effects on the input collection is ensured.

**Let's understand the entire search-inputs logging process with the following example:**

```clojure
;; We assume that the unit of time is `second`.

;; Let's say that at t1, we have the following state of the input-collection
t=1: input-collection = [{:text "world" :timestamp "t1"}]

;; At t2, a new entry comes. The input-collection building process removes the earlier entry,
;; as the earlier entry was younger than 3 secs.
;; The difference (between t1 and t2) is just 1 second.
t=2: input-collection = [{:text "world war" :timestamp "t2"}]

;; Now, let's assume that there are no new search attempts from the user.

;; At t4, the logger process runs, but does nothing because
;; the input-collection doesn't have any input older than (or equal to) 3 seconds.
;; The difference (between t2 and t4) is just 2 seconds.
t=4: input-collection = [{:text "world war" :timestamp "t2"}]

;; At t5, the logger process runs again and finds one valid input.
;; It logs the input, and removes it from the collection.
;; So, the input-collection is now empty.
t=5: input-collection = []

```
Please refer to the `effects` declaration of the **`wheninhistory.events/trigger-search-and-log`** event handler for implementation details.

## Regarding open-source
The code includes some API calls to log sessions and search inputs. In this context of API calls, some dummy texts have been put in the place of actual details, such as `token`.

The live version of [WhenInHistory](https://wheninhistory.herokuapp.com/) may have an updated version of this open-sourced code.

## High-level TODOs
- Fetch historical events from Wiki APIs.

## Feedback/Discussions
Github issues are a good way to discuss library related topics. I am also reachable via [CodeRafting](https://www.coderafting.com/).

## License
Distributed under GNU General Public License v3.0.

Copyright (c) 2020 [Amarjeet Yadav](https://www.coderafting.com/).