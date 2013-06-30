require 'rjb'

# Load JVM with given classes
Rjb::load(File.expand_path('../../lib/java/personaltt-core.jar', __FILE__)+':'+File.expand_path('../../lib/java/joda-time-2.2.jar', __FILE__))

require File.expand_path('../../lib/core/core.rb', __FILE__)
  