mkdir /home/superBits/desenvolvedor/dependenciaEmbarcada/ -p
cd /home/superBits/desenvolvedor/dependenciaEmbarcada/
git clone https://github.com/salviof/Matrix-ClientServer-API-java.git
cd /home/superBits/desenvolvedor/dependenciaEmbarcada/Matrix-ClientServer-API-java
mvn -DskipTests=true clean install
