bash "Setup Java repository" do
  user "root"
  code <<-EOF
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
apt-get update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
  EOF
  not_if { ::File.exists?("/etc/apt/sources.list.d/webupd8team-java.list") }
end

package "oracle-java8-installer" do
  action :install
end

remote_file "/tmp/sbt-0.13.9.deb" do
  source "https://dl.bintray.com/sbt/debian/sbt-0.13.9.deb"
  mode 0644
  checksum "55b849e4e991f9f524ed88fafb1e15db5ad988670654e96b01c50d0c8af55cef"
end

dpkg_package "sbt" do
  source "/tmp/sbt-0.13.9.deb"
  action :install
end

bash "Install Maven" do
  user "root"
  cwd "/opt"
  code <<-EOF
  wget http://apache.spd.co.il/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
  tar -zxf apache-maven-3.3.9-bin.tar.gz
  rm -f apache-maven-3.3.9-bin.tar.gz
  ln -sf apache-maven-3.3.9 maven
  EOF
  not_if { ::File.exists?("/opt/apache-maven-3.3.9") }
end

bash "Install Aggregator dependencies" do
  user "vagrant"
  cwd "/projects/aggregator"
  code "sbt -mem 1024 compile"
end

bash "Install Aggregator-flink dependencies" do
  user "vagrant"
  cwd "/projects/aggregator-flink"
  code "/opt/maven/bin/mvn -q compile"
end

bash "Install Dashboard dependencies" do
  user "root"
  cwd "/projects/dashboard"
  code "npm install"
end

