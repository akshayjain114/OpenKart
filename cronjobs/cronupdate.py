import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import Pbscrapper
import json

cred = credentials.Certificate('OpenKart-38e595eb11bd.json')
default_app = firebase_admin.initialize_app(cred,options={"databaseURL": "https://openkart-e703d.firebaseio.com/"})
#ref = db.reference('/users')
#print ref.get()
pbcat = db.reference('/catalogue')
mystr = Pbscrapper.writeOutput()
d = json.loads(mystr)
pbcat.set(d)
print d
#pbcat.set({"0":{"category":"BEVERAGES","category_link":"http:\/\/store.patelbros.com\/beverages\/","ProductDetails":"Assam Tea","ProductPriceRating":"$4.49","Productlink":"http:\/\/store.patelbros.com\/assam-tea\/"},"1":{"category":"BEVERAGES","category_link":"http:\/\/store.patelbros.com\/beverages\/","ProductDetails":"Brooke Bond Red Label Black Tea","ProductPriceRating":"$5.49","Productlink":"http:\/\/store.patelbros.com\/brooke-bond-red-label-black-tea\/"}})
