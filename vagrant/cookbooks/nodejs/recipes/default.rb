bash "Setup node.js repository" do
  user "root"
  code <<-EOH
    curl -sL https://deb.nodesource.com/setup_4.x > /tmp/nodejs-repo.sh
    bash /tmp/nodejs-repo.sh
    rm -f /tmp/nodejs-repo.sh
  EOH
  not_if { ::File.exists?("/etc/apt/sources.list.d/nodesource.list") }
end

package "nodejs" do
  action :install
end

