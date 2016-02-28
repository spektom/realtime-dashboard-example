bash "Install RethinkDB repository" do
  code <<-EOF
    echo "deb http://download.rethinkdb.com/apt `lsb_release -cs` main" | sudo tee /etc/apt/sources.list.d/rethinkdb.list
    wget -qO- https://download.rethinkdb.com/apt/pubkey.gpg | sudo apt-key add -
    sudo apt-get update
  EOF
  not_if { ::File.exists?("/etc/apt/sources.list.d/rethinkdb.list") }
end

package "rethinkdb" do
  action :install
end

cookbook_file "/etc/rethinkdb/instances.d/instance1.conf" do
  action :create
  owner  "root"
  mode   00644
  source "etc/rethinkdb/instances.d/instance1.conf"
  notifies :restart, "service[rethinkdb]"
end

service "rethinkdb" do
  supports :status => true, :restart => true
  action [ :enable, :start ]
end

