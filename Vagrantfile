# -*- mode: ruby -*-
# vi: set ft=ruby :

AVAILABLE_MEMORY=2048

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "hashicorp/precise64"

  config.vm.network "forwarded_port", guest: 9000, host: 9000 # playframework
  config.vm.network "forwarded_port", guest: 27017, host: 27017 # mongodb port
  config.vm.network "forwarded_port", guest: 28017, host: 28017 # mongodb admin

  # Run the provisioning script	
  config.vm.provision :shell, :path => ".vagrant_settings/setup.sh"

  config.vm.provider "virtualbox" do |vb|
     vb.customize ["modifyvm", :id, "--memory", AVAILABLE_MEMORY]
   end
end
