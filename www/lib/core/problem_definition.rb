module PersonalTimetablingAPI
  module ProblemDefinition

  	# load java classes
  	ProblemDefinitionBuilder = Rjb::import 'net.personaltt.client.ProblemDefinitionBuilder'
    #ProblemDefinitionBuilderLinkedPeriods = Rjb::import 'net.personaltt.client.ProblemDefinitionBuilder$LinkedPeriods'
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
  	def self.transitive_priority_problem(occurrences_to_reschedule, occurrences_all, priority_modes)
  		definition_builder = ProblemDefinitionBuilder.new

      closure = intersects_transitive_closure(occurrences_to_reschedule, occurrences_all)

      add_to_problem(definition_builder, closure)

      # set priority
      closure.each do |o|
        is_in_to_res = occurrences_to_reschedule.any? {|tor| tor.id == o.id}
        priority_mode = priority_modes[o.id]
        Rails.logger.debug "priority mode of #{o.id} #{is_in_to_res}: #{priority_mode}"
        definition_builder.setPreferredPriority(o.id.to_i, is_in_to_res ? (priority_mode == :repair ? 2 : 0) : 1)
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
      Rails.logger.debug "crop_to_future_of called"
      builder.cropToFutureOf PersonalTimetablingAPI::Core::Utils.to_localdatetime(until_time)
    end

     protected

    # compute transitive closure of "intersects" relation of occurrences domains
    # given in occurrencfes_all, that closure contains occurrences_seed.
    def self.intersects_transitive_closure(occurrences_seed, occurrences_all)
      Rails.logger.debug "intersects_transitive_closure called"
      # make list of domains and mapping of indexes to occurrence ids
      domains = PersonalTimetablingAPI::Core::Utils::ArrayList.new occurrences_all.size
      indexes_to_o = []
      id_to_index = {}

      # add domains to data for finding transitive closure 
      occurrences_all.each do |o|
        domains.add o.scheduling_domain.to_j
        indexes_to_o << o
        id_to_index[o.id] = indexes_to_o.length-1
      end

      # ensure that in domains is all from seed
      occurrences_seed.each do |s|
        unless id_to_index.include? s.id
          domains.add s.scheduling_domain.to_j
          indexes_to_o << s
          id_to_index[s.id] = indexes_to_o.length - 1
        end
      end

      seeds_indexes = PersonalTimetablingAPI::Core::Utils::ArrayList.new occurrences_seed.size
      occurrences_seed.each {|o| seeds_indexes.add id_to_index[o.id] }

      # call java transtive closure method
      closure_indexes = IntervalsTimeDomainUtils.computeIntersectsTransitiveClosure domains, seeds_indexes

      closure_occurrenes = PersonalTimetablingAPI::Core::Utils::j_list_to_ary(closure_indexes) do |j| 
        indexes_to_o[j.intValue()]
      end

      return closure_occurrenes
    end

    # Adds given occurrences to problem with linked groups and ordering
    def self.add_to_problem(definition_builder, occurrences) 
      #Rails.logger.debug "add_to_problem called"
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

      end
      #Rails.logger.debug "add_to_problem ended"

      definition_builder
    end
end
end