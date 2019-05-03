ssh root@192.168.1.27 "
rm -rf Bully;
git clone https://github.com/alvarotorijano/Bully.git;
cd Bully;
git checkout deploy;
./Bully/Bully/deploy/Launch.sh
"