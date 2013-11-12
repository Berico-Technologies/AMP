Name:		gts
Version:	3.2.1
Release:	1%{?dist}
Summary:	Global Topology Service

Group:		Development/Libraries
License:	Apache License, Version 2.0
URL:		https://github.com/Berico-Technologies/AMP
Source0:	amp.topology.service-%{version}.jar
Source1:	gts-configuration-%{version}.tgz
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

gzip -cd %{SOURCE1} | tar --strip-components=1 -C %{buildroot}/etc/gts -x

install -m 0755 %{SOURCE2} %{buildroot}/etc/init.d/

install -m 0644 %{SOURCE0} %{buildroot}/opt/gts/


%files
%defattr(-,root,root,-)
%config(noreplace) /etc/gts/*
/etc/init.d/*
/opt/gts
%doc



%changelog

