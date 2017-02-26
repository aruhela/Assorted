# Assorted
K-Means Clustering 
This repository contain my implementation of K-Means algorithm that is optimized for finding cluster of geographical locations. Both the Source Code and Outputs of running the source code with 50000 and 4 Million locations is included in the repository. The details of my algorithm, contributions, limitation and results of the algorithm are specified in the Readme.txt file.

# Assorted/Bitly
The limitation on length of tweets compels user to shorten URLs in their tweets. I have used a python script to resolve short URLs to their extended form. Nearly 2 Million URLs were parsed by this Python script. 
The purpose of short URL expansion is two folds.
[1] To find which URLs points to the same target page (Extended URL).  – To infer Tweets cascade.
[2] To study the characteristic of topics spread temporally and spatially.


# Assorted/Elsevier
There are several source files in this folder. These have been used to study and contrast the behavior of Celebrity users against the Regular users on twitter. Nearly 468 Million Tweets posted by 26 Million Users are studied in this project. The work is published in Elsevier COMCOM journal.
Title  : “The rich and middle classes on Twitter: Are popular users indeed different from regular users” 
URL  : http://www.sciencedirect.com/science/article/pii/S0140366415002625


# Assorted/Hadoop
To get the frequency of all the topics in a dataset of size 18GB, I installed and run the hadoop library on a cluster of two Linux machines and retrieve the topics frequency in a single run. This has significantly reduced our coding efforts and processing time.  


# Assorted/Yahoo
We observed that only 61% of all 7.39M users in our dataset supplied their location information. No common format was used though. Therefore, we first converted all the extracted locations into a common format as a pair of latitude and longitude coordinates, and then we reverse converted the coordinates to a triplet of city, state and country. We considered options of Yahoo! Placefinder, Google Geocoding and the MapQuest Geocoding APIs to geocode the extracted locations. Since Yahoo provided the maximum free rate limit of 50,000 requests per IP addresses/day, we used it to geocode all locations.

# Assorted/NLP
Manual identification of topics in a dataset of 196 Million tweets is impossible. We, therefore, used Reuters OpenCalais web service to automatically extract topics (entities and tags) from all tweets. Since OpenCalais was rate limited, therefore optimum bundle of tweets were queried to OpenCalais. The RDF response from OpenCalais parsed to get topics from the tweets. We obtained 39 million URLs and 7.5 million unique topics from OpenCalais.



