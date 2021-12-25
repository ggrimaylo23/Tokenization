# Tokenization
This program is a tokenization system. It implements tokenization, stopword removal, and the first two steps of Porter stemming. 
The system can run on a document, and then outputs the result to a file. It can also generate a list of the terms and their frequencies and put them in an output file.
Tokenization: abbreviations are turned into one term. Ex: "U.S.A." becomes "USA". All other punctuation is just considered a word separator. Ex: "500.00" becomes ["500", "00"]. Lowercase all letters. 
Stopword removal is implemented. You can use your own stopwords.txt file. 
The first two steps of Porter stemming is implemented (see http://snowball.tartarus.org/algorithms/porter/stemmer.html). 
