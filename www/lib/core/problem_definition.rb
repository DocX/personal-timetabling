module PersonalTimetablingAPI
  module ProblemDefinition

  	# load java classes
  	ProblemDefinitionBuilder = Rjb::import 'net.personaltt.client.ProblemDefinitionBuilder'
    ProblemDefinitionBuilderLinkedPeriods = Rjb::import 'net.personaltt.client.ProblemDefinitionBuilder$LinkedPeriods'
  	ScheduleParser = Rjb::import 'net.personaltt.client.ScheduleParser'
    IntervalsTimeDomainUtils = Rjb::import 'net.personaltt.timedomain.IntervalsTimeDomainUtils'


    # Creates problem definition builder and prefill it with 
    # given occurrences to reschedule and its domain intersecting
    # transitive closure. 
    #
    # 2 modes of priority is supported:
    # - :repair : 
    #   has higher priority than not in given list and with added, 
    #   causing it should stay at preferred place
    # - :added : 
    #   has lower priority than not in given list and with repair mode, causing it will wind best place in 
    #   context of oters
    # priority_modes is map of id to priority mode symbol
    #
    # Ordering preferrences are also defined
  	def self.transitive_priortiy_problem(occurrences_to_reschedule, occurrences_all, priority_modes)
  		definition_builder = ProblemDefinitionBuilder.new

      closure = intersects_transitive_closure(occurrences_to_reschedule, occurrences_all)

      add_to_problem(definition_builder, closure)

      # set priority
      closure.each do |o|
        is_in_to_res = occurrences_to_reschedule.any? {|tor| tor.id == o.id}
        priority_mode = priority_modes[o.id]
        definition_builder.setPreferredPriority(is_in_to_res ? (priority_mode == :repair ? 2 : 0) : 1)
      end

  		definition_builder
  	end

    # Creates problem builder with given occurrences to schdule them 
    # by its preferred allocation. no priority is applied
    def self.global_reschedule_problem(occurrences_to_reschedule)
      definition_builder = ProblemDefinitionBuilder.new

      add_to_problem(definition_builder, occurrences_to_reschedule)

      definition_builder
    end

    def self.crop_to_future_of(builder, until_time) 
      builder.cropToFutureOf PersonalTimetablingAPI::Core::Utils.to_localdatetime(until_time)
    end

     protected

    # compute transitive closure of "intersects" relation of occurrences domains
    # given in occurrencfes_all, that closure contains occurrences_seed.
    def self.intersects_transitive_closure(occurrences_seed, occurrences_all)
      # make list of domains and mapping of indexes to occurrence ids
      domains = PersonalTimetablingAPI::Core::Utils::ArrayList.new occurrences_all.size
      indexes_to_id = []
      id_to_index = {}

      # add domains to data for finding transitive closure 
      occurrences_all.each do |o|
        domains.add o.scheduling_domain.to_j
        indexes_to_id << o.id
        id_to_index[o.id] = indexes_to_id.length-1
      end

      seeds_indexes = PersonalTimetablingAPI::Core::Utils::ArrayList.new occurrences_seed.size
      occurrences_seed.each {|o| seeds_indexes.add id_to_index[o.id] }

      # call java transtive closure method
      closure_indexes = IntervalsTimeDomainUtils.computeIntersectsTransitiveClosure domains, seeds_indexes

      closure_occurrenes = PersonalTimetablingAPI::Core::Utils::j_list_to_ary(closure_indexes) do |j| 
        occurrences_all.find {|o| o.id == indexes_to_id[j.intValue()] }
      end

      return closure_occurrenes
    end

    # Adds given occurrences to problem with linked groups and ordering
    def self.add_to_problem(definition_builder, occurrences) 
      linked_groups = {}

      occurrences.each do |o|
        # add occurrence from closure definition
        definition_builder.addOccurrence( 
          o.id, 
          PersonalTimetablingAPI::Core::Utils.to_localdatetime(o.start.to_datetime), 
          o.duration, 
          o.min_duration, 
          o.max_duration,
          o.scheduling_domain.to_j
        )

        # prepare linked groups
        if (not o.activity.nil? and o.activity.linked?)
          linked_groups[o.activity.id] ||= {:activity => o.activity, :occurrences => []}
          linked_groups[o.activity.id][:occurrences] << o
        end

        # add order
        o.ordered_after_this_ids.each do |oa|
          definition_builder.addOrderPreferrence(o.id, oa.id)
        end
      end

      # add linked groups
      linked_groups.each do |aid, g|
        case g[:activity].link_comparator
        when :time_in_day
          linked_period = ProblemDefinitionBuilderLinkedPeriods.TIME_IN_DAY
        when :day_and_time_in_week
          linked_period = ProblemDefinitionBuilderLinkedPeriods.DAY_AND_TIME_IN_WEEK
        when :day_and_time_in_month
          linked_period = ProblemDefinitionBuilderLinkedPeriods.DAY_AND_TIME_IN_MONTH
        end

        definition_builder.setLinkedOccurrences( (g[:occurrences].map {|o| o.id}), linked_period)
      end

      definition_builder
    end
end
end