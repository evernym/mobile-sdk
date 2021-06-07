require "json"

Pod::Spec.new do |s|
  s.name         = "evernym-react-native-sdk"
  s.version      = "0.0.1"
  s.summary      = "React Native version of Evernym's VCX."
  s.description  = "React Native wrapper for vcx"
  s.homepage     = "https://gitlab.com/evernym/mobile/mobile-sdk.git"
  # brief license entry:
  s.license      =  { :type => 'MIT', :file => 'LICENSE' }
  s.authors      = { "Evernym Inc." => "info@evernym.com" }
  s.platforms    = { :ios => "10.0" }
  s.source       = { :http => 'https://gitlab.com/evernym/mobile/mobile-sdk/-/package_files/11657102/download', :type => 'zip' }
  s.swift_version = '4.0'

  s.source_files = "ios/**/*.{h,c,m,swift}"
  s.requires_arc = true

  s.dependency "React-Core"
  s.dependency "vcx"

end
