# Base activity definition.
# Defines how to create events for activity and updates them when definition is updated.
# And loading and saving interchange format for API

module ActivityDefinition
	class BaseActivityDefinition
		include ActiveModel::Validations
		include ActiveModel::Conversion
		extend ActiveModel::Naming
		extend ActiveRecord::Validations::ClassMethods

		def self.from_attributes(attributes)
			klass = ('ActivityDefinition::' + (attributes[:type].downcase.underscore + '_activity_definition').classify).constantize
			definition = klass.from_attributes attributes

			definition
		end

		def create_events(activity)
			raise 'not implemented'
		end

		def update_events(activity)
			raise 'not implemented'
		end

		def to_attributes
			raise 'not implemented'
		end


		def persisted?
			false
		end

		def marked_for_destruction?
			false
		end

		def _destroy
			0
		end

	end
end