# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20130702185719) do

  create_table "activities", :force => true do |t|
    t.string   "name",                               :null => false
    t.text     "definition"
    t.boolean  "link_events",     :default => false, :null => false
    t.string   "link_comparator"
    t.datetime "created_at",                         :null => false
    t.datetime "updated_at",                         :null => false
  end

  create_table "domain_templates", :force => true do |t|
    t.string  "name"
    t.text    "domain"
    t.integer "reference_count", :default => 0, :null => false
  end

  create_table "events", :force => true do |t|
    t.datetime "start",                            :null => false
    t.integer  "duration",                         :null => false
    t.datetime "end",                              :null => false
    t.integer  "activity_id"
    t.integer  "min_duration",                     :null => false
    t.integer  "max_duration",                     :null => false
    t.integer  "tz_offset",         :default => 0, :null => false
    t.string   "name",                             :null => false
    t.text     "domain"
    t.text     "events_after"
    t.datetime "schedule_since",                   :null => false
    t.datetime "schedule_deadline",                :null => false
    t.datetime "created_at",                       :null => false
    t.datetime "updated_at",                       :null => false
  end

end
