require "json"

Pod::Spec.new do |s|
  s.name         = "react-native-vcx-wrapper"
  s.version      = "0.0.1"
  s.summary      = "React Native version of Evernym's VCX."
  s.description  = "React Native wrapper for vcx"
  s.homepage     = "https://gitlab.com/evernym/mobile/mobile-sdk.git"
  # brief license entry:
  s.license      =  { :type => 'MIT', :file => 'LICENSE' }
  s.authors      = { "Evernym Inc." => "info@evernym.com" }
  s.platforms    = { :ios => "10.0" }
  s.source       = { :git => "https://gitlab.com/evernym/mobile/mobile-sdk.git", :tag => "#{s.version}" }
  s.swift_version = '4.0'

  s.source_files = "ios/**/*.{h,c,m,swift}"
  s.requires_arc = true

  s.dependency "React-Core"
  s.dependency "vcx"

end
