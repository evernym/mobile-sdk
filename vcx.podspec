Pod::Spec.new do |s|
  s.name             = 'vcx'
  s.version          = '0.0.165'
  s.summary          = 'The Objective-C wrapper around the libvcx shared library.'

  s.description      = <<-DESC
The ConnectMe mobile app on the iOS platform will call into the libvcx shared library
from Objective-C. This pod is a very thin Objective-C wrapper that allows react native to call
through to the libvcx shared library.
                       DESC

  s.homepage         = 'https://www.evernym.com/'
  s.author           = { 'evernym-ios-dev' => 'iosdev@evernym.com' }
  s.source           = { :http => 'file:' + __dir__ + '/vcx.libvcxall_0.7.69722834-fb364dde0_universal.zip' }

  s.ios.deployment_target = '8.0'
  s.ios.vendored_frameworks="vcx/vcx.framework"
  s.compiler_flags = '-ObjC'
  s.public_header_files = 'vcx/vcx.framework/include/*.h'
  s.ios.vendored_library = 'vcx/vcx.framework/lib/libvcx.a'
end