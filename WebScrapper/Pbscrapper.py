#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Mar 30 00:40:44 2018

@author: surabhisudame
"""
import pandas as pd
import requests
import inspect
import os
from bs4 import BeautifulSoup
from bs4 import NavigableString
 

def getele(left): 
    for ele in left.children:
            if isinstance(ele, NavigableString):
                continue
            else :
                 #print(ele.div)
                 print(ele.find(class_= "BlockContent"))
                
        

def findcategorylist(soup):
    newDF = pd.DataFrame(columns=['category', 'link'])
    i = 1;
    #newDF = pd.DataFrame()
    soup.select("class_  SideCategoryListFlyout")
    Categorybar = soup.find(id="SideCategoryList")
    unlist =  Categorybar.find('ul')
    for ele in unlist.children:
        if isinstance(ele, NavigableString):
            continue
        else :
            #print(ele)
            list = ele.find('a').get_text()
            link = ele.find('a')['href']
            #print(ele.find('a')['href'])
            newDF.loc[i] = [list , link ]
            i = i+1;
           # print(list)
    return newDF  
      
def fetchallpagelinks(category_link):
    pagelinks = list()
    pagelinks.append(category_link)
    
    category_page = requests.get(category_link)
    category_soup = BeautifulSoup(category_page.content, 'html.parser')
    
    get_pages = category_soup.find(id="CategoryPagingTop")
    pages_list = list(get_pages.children)
    #print(pages_list[1].find_all('a'))
    
    if pages_list:
        remainingpages = get_pages.find('ul')
        for page in remainingpages.find_all('a'):
            pagelinks.append(page['href'])
   
    return pagelinks
      
def fetchdatafromcategory(category_link, category):
    
   
    items = pd.DataFrame(columns=['category', 'category_link', 'ProductDetails', 'ProductPriceRating', 'Productlink'])
    pagelinks = fetchallpagelinks(category_link)
   # print(pagelinks)
    # print("\n\n\n")
    it =1;
    
    for link in pagelinks:
        page = requests.get(link)
        page_soup = BeautifulSoup(page.content, 'html.parser')
        pagecontent = page_soup.find(id="CategoryContent")
        product_list = pagecontent.find('ul')
        for ele in product_list.children:
            if isinstance(ele, NavigableString):
                continue
            else :
                ProductPriceRating = ele.find(class_ = "ProductPriceRating")
                ProductDetails = ele.find(class_ = "ProductDetails")
                itemlink = ele.find('a')['href']
                items.loc[it] = [category, category_link, ProductDetails.get_text().strip(), ProductPriceRating.get_text().strip(), itemlink ] 
                it = it+1
    return items
    
    
    
def fetchallitemsfromcategories(category_frame):
   items = pd.DataFrame(columns=['category', 'category_link', 'ProductDetails', 'ProductPriceRating', 'Productlink'])
   for index, row in category_frame.iterrows():
        category_link = row['link']
        
        df = fetchdatafromcategory(category_link, row['category'] ) 
        frames = [items, df] 
        items = pd.concat(frames, axis=0, ignore_index=True, keys=None, levels=None, names=None, verify_integrity=False)
    
   return items    
                

    
###########_____Driver Code______############## 
#output file name
filename = inspect.getframeinfo(inspect.currentframe()).filename
path = os.path.dirname(os.path.abspath(filename))
file_name = path + "/pbdata.csv"
page = requests.get("http://store.patelbros.com/")
soup = BeautifulSoup(page.content, 'html.parser')
# scrapping categories
category_frame = findcategorylist(soup)
finalitems = fetchallitemsfromcategories(category_frame)
#finalitems.dtypes
finalitems.to_csv(file_name, sep=',', encoding='utf-8', index=False)
for p in finalitems: print p





#getcategorydetails(categorybarcontent)

#getele(left)

####### Analyzing Page ##
#In
#print(soup.prettify())
#mylist  = list(soup.children)
#html = mylist[2]
#list(html.children)
#body = list(html.children)[3]

#body
#container = list(body.children)[1]
#outer = list(container.children)[5]
#wrapper = list(outer.children)[11]
#wrapper
#type(wrapper)       
#left = list(wrapper.children)[1]
#list(left.children)
#len(left)
#categorybar = list(left.children)[1]
#categorybarcontent = list((list(categorybar.children)[3]).children)[1]
#categorybarcontent
