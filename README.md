# HackerRank Submissions Downloader 

[![Build Status](https://travis-ci.org/hermes-jr/hackerrank-downloader.svg?branch=master)](https://travis-ci.org/hermes-jr/hackerrank-downloader)

## Usage

```
java -jar hackerrank-downloader.jar [-d <PATH>] [-f] [-h] [-l <NUMBER>] [-o <NUMBER>] [-v]
 -d,--directory <PATH>   path to output directory. Default: current
                         working directory
 -f,--force-overwrite    Force overwrite if output directory exists. May
                         lead to data loss.
 -h,--help               display this help and exit
 -l,--limit <NUMBER>     number of solved challenges to download. Default
                         is 65535
 -o,--offset <NUMBER>    number of items to skip. Default is 0
 -v,--verbose            run in verbose mode

Application expects a file .hackerrank-downloader-key to exist in your
home directory. It must contain a single ASCII line, a value of
"_hrank_session" cookie variable
```
