import re
import urllib.request
import string
#the source file of the tweets to be resolved !
inputFile = 'SrcFile.txt'

#the output file of the tweets resolved !
outputFile ='resolvedTweets.txt'

#the Mapping  of the URLS tweets resolved so far!
resolvedMapping ='mapping'

Mapping = {'short': 'longurl'}

fmap = open(resolvedMapping,'a')
fout = open(outputFile,'w')


def resolveUrl() :
    repeaturl =0
    NotValidURL =0
    newurl    =0
    f = open(inputFile)
    url = f.readline();

    while url:
      url = url.replace('"','')
      url = url.replace('\n','');
#      print(url+"");
      response =''
      
      try :
            if url[0]=="h":
               if(Mapping.get(url)!=None):
                  response = Mapping.get(url)
                  repeaturl = repeaturl +1
               else :
                  response = urllib.request.urlopen(url)
                  response = response.geturl()
                  Mapping[url] =response
                  newurl = newurl +1
                  fmap.write(url+'\t'+response+'\n')  
            else :
               response = 'none'
               NotValidURL = NotValidURL +1
      except :
            print("Unexpected error")	  
            response = 'UnexpectedError'
                  
      print(url+"\t"+response+'\n\n')
      fout.write(url+"\t"+response+'\n')
      url = f.readline()

    # Write Stats	  
    print('repeat : newurl : NotValidURL',repeaturl,newurl,NotValidURL)


resolveUrl()

