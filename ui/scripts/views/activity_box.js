// box moving and resizing in column view
$.widget("pt.activity_occurance_box", $.pt.column_box, {
  
  _create: function() {
    this.options = $.extend(this.options, {
      drag: _.bind(this.activity_box_moved,this),
      stop: _.bind(this.activity_box_moving_stop, this),
      start: this.options.occurance.get('start'),
      duration: this.options.occurance.get('duration')
    })
    this.element
      .attr('data-type', 'activity-occurance')
      .addClass('activity-occurance')
      .append(this.template);
    
    this.occurance = this.options.occurance;
      
    this.element.find('[data-source=name]').text(this.options.occurance.get("activity").get("name")); 
    var start_date = this.options.occurance.get("start").format(this.activity_date_format);
    var end_date =  this.options.occurance.get("end").format(this.activity_date_format);
    
    this.element.find('[data-source=start]').text(start_date);
    this.element.find('[data-source=end]').text(end_date);

    this.element.tooltip({
      title: 
      this.options.occurance.get("activity").get("name") +
      '<br>' +
      start_date + 
      ' - ' +
      end_date,
      html: true,
      placement: this.options.view.axis == 'x' ? 'right' : 'top',
      animation: false
    });
    
   /* this.element.find('a[data-button=activity-occurance-btn-edit]')
      .click(_.bind(_.partial(this._trigger,'edit', null, {occurance_id: this.occurance.get('id'), occurance: this.occurance, element:this.element}), this));
    this.element.find('a[data-button=activity-occurance-btn-remove]')
      .click(_.bind(_.partial(this._trigger,'remove', null, {occurance_id: this.occurance.get('id'), occurance: this.occurance, element:this.element}), this));*/
    
    this._super();      
  },
  
  template: 
    ""+
      "<div class='activity-occurance-inner'>" +
        "<div class='columnbox-handle-front activity-occurance-handle'></div>" +
        "<div class='columnbox-handle-back activity-occurance-handle'></div>" +
        "<div class='activity-occurance-labels'>" +
          "<div class='name' data-source='name'></div>" +
        "</div>" +
      "</div>" +
    "",
 
  _check: function(start, duration){
    var occurance = this.options.occurance;
    //check against domain
    if (occurance.domain_intervals && !occurance.domain_intervals.isFeasible(start, duration)) {
      return false;
    }

    //check against duration limits
    if (!occurance.validDuration(duration)) {
      return false;
    }

    return true;
  },

  activity_date_format: 'DD.MM.YY H:mm',
  
  activity_box_moved: function(e, ui) {
    var $box = $(ui.el);
    var occurance = this.options.occurance;
    var column = this.column;

    occurance.set({
      start: ui.date_front,
      duration: ui.duration
    });
    
    var start_date = ui.date_front.format(this.activity_date_format);
    var end_date =  occurance.get('end').format(this.activity_date_format);
    $box.find('[data-source=start]').text();
    $box.find('[data-source=end]').text();
    
    $box.attr('data-original-title',
      occurance.get("activity").get("name") +
      '<br>' +
      start_date + 
      ' - ' +
      end_date
     ).tooltip('fixTitle').tooltip('show');
    
    // align to new date time
    return true;
  },

  activity_box_moving_stop: function(e, ui) {
    this.occurance.save();
  },

  getOccurance: function() {
    return this.occurance;
  }
});