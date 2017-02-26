# The program expends the short URLs of Bitly to Long URLs
# Author : Amit Ruhela
import re
import urllib
import string

#the source file of the tweets to be resolved
inputFile = 'SrcFile.txt'

#the output file of the tweets resolved
outputFile ='ResolvedUrlsFile'


#the Mapping  of the URLS tweets resolved so far
resolvedMapping ='mapping'
Mapping = {'short': 'longurl'}

fmap = open(resolvedMapping,'a')
fout = open(outputFile,'a')

def resolveUrl() :
    repeaturl =0
    nourl     =0
    newurl    =0
    
    f = open(inputFile)
    line = f.readline();
    data = line;

    while line:
      line = re.split('-|||-',line)
      url = line[6]
      url = url.replace('"','')
      data = data.replace('\n','')
      response =''
      
      try :
            if url[0]=="h":
               if(Mapping.get(url)!=None):
                  response = Mapping.get(url)
                  repeaturl = repeaturl +1
               else :
                  response = urllib.urlopen(url)
                  response = response.geturl()
                  Mapping[url] = response
                  newurl = newurl +1
                  fmap.write(url+'\t'+response+'\n')  
            else :
               response = 'none'
               nourl = nourl +1
      except :
            response = 'none'
            return
      
      fout.write(data+"-|||-"+response+'\n')
      line = f.readline()
      data = line

resolveUrl()

