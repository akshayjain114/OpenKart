Create Virtual Instance using Ubuntu in any cloud service.

1) And run the following commands
sudo apt-get install python-setuptools python-dev build-essential
sudo easy_install pip
pip install pipreqs
sudo pip install pipreqs

2) Create a directory
3) Upload the scripts to Pbscrapper.py and cronupdate.py to this new directory
4) Now execute "cd" to the newly created directory
5) Then run the following commads
pipreqs ./

6) there is a new file named requirements.txt created open this file and change the version of "requests" package to 18.0.0
7) then run this command
sudo pip install -r requirements.txt

8) then run the following command
crontab -e
select any editor and paste the following line without the double quotes
"*/59 * * * * /usr/bin/python cronupdate.py"
