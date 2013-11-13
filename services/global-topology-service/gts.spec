Name:		gts
Version:	3.2.1
Release:	1%{?dist}
Summary:	Global Topology Service

Group:		Development/Libraries
License:	Apache License, Version 2.0
URL:		https://github.com/Berico-Technologies/AMP
# from target/ directory of global-topology-service build
Source0:	amp.topology.service-%{version}.jar
# tarred configuration/ directory (from target/ directory) of g-t-s build
Source1:	gts-configuration-%{version}.tgz
# in AMP github feature/sysvinit
Source2:	gts.init
BuildArch:	noarch

# BuildRequires:	
# Requires:	

%description
AMP is many things, but all related to messaging:

Implementation of CMF (http://github.com/Berico-Technologies/CMF) using AMQP.
Centrally managed messaging topology (Global Topology).
Compendium of useful Event and Envelope Processors.
Toolchain around AMQP/CMF (Maven Archetypes, Monitoring tools, etc.)

%prep

%build

%install
install -d %{buildroot}/etc/gts
install -d %{buildroot}/etc/init.d
install -d %{buildroot}/opt/gts

gzip -cd %{SOURCE1} | tar -C %{buildroot}/etc/gts -x

mv %{buildroot}/etc/gts/configuration/gts-* %{buildroot}/etc/gts/

install -m 0755 -D %{SOURCE2} %{buildroot}/etc/init.d/%{name}

install -m 0644 %{SOURCE0} %{buildroot}/opt/gts/

%pre
getent group gts > /dev/null || groupadd -r gts
getent passwd gts > /dev/null || \
  useradd -r -g gts -d /opt/gts -s /bin/bash \
  -c "Global Topology Service user" gts

%post

chkconfig --add %{name}

%files
%defattr(-,root,root,-)
%dir %attr(755,gts,root) /etc/gts
%config(noreplace) /etc/gts/*
/etc/init.d/*
%attr(755,gts,root) /opt/gts
%doc



%changelog

