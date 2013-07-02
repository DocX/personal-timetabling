# Domain template domain. Virtual domain that uses domain stored in domain template
# in database
module TimeDomains
	class DomainTemplateTimeDomain < BaseTimeDomain
		include PersonalTimetablingAPI::Core::IntervalsSetBridge

		attr_accessor :domain_template_id

		def initialize
			@domain_template_id = nil
		end

		def self.from_attributes attributes
			domain_template_time_domain = DomainTemplateTimeDomain.new
			domain_template_time_domain.domain_template_id = attributes[:id]

			domain_template_time_domain
		end

		def referenced_domain_templates_ids
			[@domain_template_id]
		end

		def encode_with coder
			coder['domain_template_id'] = @domain_template_id
		end

		def init_with coder
			@domain_template_id = coder['domain_template_id']
		end

		def to_hash
			{
				:type => 'domain_template',
				:data => {:id => @domain_template_id}
			}
		end

		# get java domain object
		def to_j
			get_original_domain.to_j
		end

		def get_original_domain
			domain_template = DomainTemplate.find @domain_template_id
			domain_template.domain
		end
	end
end