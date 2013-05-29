# Time domain representes set of intervals in which some time variable (activity occurence) can occure
# This implementation is using concept of stack of interval templates actions. On the bottom of the stack, no time is defined. Each
# action in the stacks adds, removes or masks time intervals available in the level bellow. 
#
# First item in stack is at the top (applying on the next item's domain)

class TimeDomainStack < TimeDomain
  include Webui::Core::TimeDomainStackMixin
  
  class Action
    ADD = 1
    REMOVE = 2
    MASK = 3
    
    attr_accessor :action, :time_domain
    
    def initialize(action, domain) 
      throw 'Unsupported action' if not [ADD, REMOVE, MASK].include? action
      throw 'Action can be defined only with TimeDomain object, %s given'%domain.class.name unless domain.is_a? TimeDomain
      
      @action = action
      @time_domain = domain
    end
    
    # parse from form attributes
    def self.from_attributes attributes
      action = {'add' => ADD, 'remove' => REMOVE, 'mask' => MASK}[attributes['action']]
      domain = TimeDomain.from_attributes attributes['domain']
      
      self.new action, domain
    end
    
    def human_action
      {ADD => 'Add', REMOVE => 'Remove', MASK => 'Mask'}[@action]
    end
    
    def encode_with coder
      coder['action'] = @action.to_i
      coder['time_domain'] = @time_domain
    end
    
    def init_with coder
      @action = coder['action'] || 1
      @time_domain = coder['time_domain']
    end
  end
  
  attr_accessor :actions_stack

  def initialize
    super
    @actions_stack = []
  end
  
  # adds action below all others, so it will be computed first time and sent to the upper action
  def push(action)
    throw 'Only TimeDomainStack::Action objects can be pushed' unless action.is_a? Action

    @actions_stack << action;
  end

  # adds action above all others, so it will be applied last time to the result of below actions
  def unshift(action) 
    throw 'Only TimeDomainStack::Action objects can be pushed' unless action.is_a? Action

    @actions_stack.unshift action
  end
  
  def self.from_attributes attrs
    actions = attrs['actions'].map {|v| Action.from_attributes v}
    
    domain = self.new 
    domain.actions_stack = actions
    domain
  end
  
  def encode_with coder
    coder['actions_stack'] = self.actions_stack || []
  end

  def init_with coder
    self.actions_stack = coder['actions_stack'] || []
  end
end