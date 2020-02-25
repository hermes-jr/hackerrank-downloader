# HackerRank Submissions Downloader 

[![Build Status](https://travis-ci.org/hermes-jr/hackerrank-downloader.svg?branch=master)](https://travis-ci.org/hermes-jr/hackerrank-downloader)
![Java CI](https://github.com/hermes-jr/hackerrank-downloader/workflows/Java%20CI/badge.svg?branch=master)

## Usage

```
usage: java -jar hackerrank-downloader.jar [-d <PATH>] [-f] [-h] [-l <NUMBER>] [-o <NUMBER>] [-p <PASSWORD>] [-u <USERNAME>] [-v]
 -d,--directory <PATH>      path to output directory. Default: current
                            working directory
 -f,--force-overwrite       Force overwrite if output directory exists.
                            May lead to data loss.
 -h,--help                  display this help and exit
 -l,--limit <NUMBER>        number of solved challenges to download.
                            Default is 65535
 -o,--offset <NUMBER>       number of items to skip. Default is 0
 -p,--password <PASSWORD>   HackerRank account password
 -u,--username <USERNAME>   HackerRank account username or email
 -v,--verbose               run in verbose mode

If you are experiencing problems with JSON parser, try to run the program
with "-Dfile.encoding=UTF-8" option

Application expects either a file .hackerrank-downloader-key to exist in
your home directory or a --username and --password to be provided.

.hackerrank-downloader-key is an option if you are using OAuth to sign in.
Just authenticate using your browser and save "_hrank_session" cookie
value into the file.
```
