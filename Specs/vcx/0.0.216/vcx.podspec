#
# Be sure to run `pod lib lint vcx.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

# NOTE: UPDATE ME: versions of libraries in this cocoapod
# libindy 1.16.0~170 master
# libvcx 0.12.0-26a645ad

Pod::Spec.new do |s|
  s.name             = 'vcx'
  s.version          = '0.0.216'
  s.summary          = 'The Objective-C wrapper around the libvcx shared library for simulators.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
The ConnectMe mobile app on the iOS platform will call into the libvcx shared library
from Objective-C. This pod is a very thin Objective-C wrapper that allows react native to call
through to the libvcx shared library.
                       DESC

  s.homepage         = 'https://www.evernym.com/'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'evernym-ios-dev' => 'iosdev@evernym.com' }
  s.source           = { :http => 'https://gitlab.com/evernym/mobile/mobile-sdk/-/package_files/13728816/download', :type => 'zip' }

  s.ios.deployment_target = '8.0'

  #s.source_files = '**/vcx/Classes/**/*','**/Example/Classes/**/*'

  # s.resource_bundles = {
  #   'vcx' => ['**/vcx/Assets/*.png']
  # }
  s.ios.vendored_frameworks="vcx/vcx.framework"
  s.compiler_flags = '-ObjC'
  s.public_header_files = 'vcx/vcx.framework/include/*.h'
  s.ios.vendored_library = 'vcx/vcx.framework/lib/libvcx.a'
  # s.frameworks = 'UIKit', 'MapKit'
  # s.dependency 'AFNetworking', '~> 2.3'
end
