cookbook_file "/etc/sysctl.d/local.conf" do
  source   "sysctl.conf"
  owner    "root"
  group    "root"
  mode     00644
  notifies :run, "execute[/sbin/sysctl -p]", :delayed
end

execute "/sbin/sysctl -p" do
  action :nothing
end

cookbook_file "/etc/security/limits.d/local.conf" do
  source   "limits.conf"
  owner    "root"
  group    "root"
  mode     00644
end
