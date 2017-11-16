var VueDatetime = (function () {
  'use strict';

  function weekdays(locale, mondayFirst) {
    if (mondayFirst === void 0) mondayFirst = false;

    var weekdays = moment.localeData(locale).weekdaysMin();
    if (mondayFirst) {
      return weekdays.slice(1).concat(weekdays[0])
    }
    return weekdays
  }

  function monthDays(month, year, mondayFirst) {
    if (mondayFirst === void 0) mondayFirst = false;

    var monthDate = moment([year, month, 1]);
    var firstDay = monthDate.day() - (mondayFirst ? 1 : 0);

    if (firstDay === -1) {
      firstDay = 6;
    }

    var days = (new Array(monthDate.daysInMonth() + firstDay)).fill(null);

    return days.map(function (value, index) {
      return index + 1 < firstDay ? null : index + 1 - firstDay
    })
  }

  function years() {
    var currentYear = moment().year();
    var years = [];

    for (var i = currentYear - 100; i < currentYear + 100; i++) {
      years.push(i);
    }

    return years
  }

  function hours() {
    var hours = [];

    for (var i = 0; i < 24; i++) {
      hours.push(i < 10 ? '0' + i : i);
    }

    return hours
  }

  function minutes() {
    var minutes = [];

    for (var i = 0; i < 60; i++) {
      minutes.push(i < 10 ? '0' + i : i);
    }

    return minutes
  }

  var TypeFlow = function TypeFlow(component, date) {
    this.component = component;
    this.date = date;
  };

  TypeFlow.prototype.setDate = function setDate(date) {
    this.date = date;
  };

  TypeFlow.prototype.open = function open() { };

  TypeFlow.prototype.close = function close() { };

  TypeFlow.prototype.ok = function ok() {
    this.component.close();
  };

  TypeFlow.prototype.selectDay = function selectDay(year, month, day) {
    this.date
      .year(year)
      .month(month)
      .date(day);
  };

  TypeFlow.prototype.selectHour = function selectHour(hour) {
    this.date.hour(hour);

    if (this.timeSelected && this.component.autoClose) {
      this.component.close();
    }

    this.timeSelected = true;
  };

  TypeFlow.prototype.selectMinute = function selectMinute(minute) {
    this.date.minute(minute);

    if (this.timeSelected && this.component.autoClose) {
      this.component.close();
    }

    this.timeSelected = true;
  };

  TypeFlow.prototype.format = function format(date, format$1) {
    return date ? date.format(format$1) : ''
  };

  TypeFlow.prototype.isoDate = function isoDate() {
    return this.date.toISOString()
  };

  var DateTypeFlow = (function (TypeFlow$$1) {
    function DateTypeFlow() {
      TypeFlow$$1.apply(this, arguments);
    }

    if (TypeFlow$$1) DateTypeFlow.__proto__ = TypeFlow$$1;
    DateTypeFlow.prototype = Object.create(TypeFlow$$1 && TypeFlow$$1.prototype);
    DateTypeFlow.prototype.constructor = DateTypeFlow;

    DateTypeFlow.prototype.inputFormat = function inputFormat() {
      return 'YYYY-MM-DD'
    };

    DateTypeFlow.prototype.open = function open() {
      this.component.showDatePicker();
    };

    DateTypeFlow.prototype.selectDay = function selectDay(year, month, day) {
      TypeFlow$$1.prototype.selectDay.call(this, year, month, day);

      if (this.component.autoClose) {
        this.component.close();
      }
    };

    return DateTypeFlow;
  }(TypeFlow));

  var DatetimeTypeFlow = (function (TypeFlow$$1) {
    function DatetimeTypeFlow() {
      TypeFlow$$1.apply(this, arguments);
    }

    if (TypeFlow$$1) DatetimeTypeFlow.__proto__ = TypeFlow$$1;
    DatetimeTypeFlow.prototype = Object.create(TypeFlow$$1 && TypeFlow$$1.prototype);
    DatetimeTypeFlow.prototype.constructor = DatetimeTypeFlow;

    DatetimeTypeFlow.prototype.inputFormat = function inputFormat() {
      return 'YYYY-MM-DD HH:mm'
    };

    DatetimeTypeFlow.prototype.open = function open() {
      this.component.showDatePicker();
      this.timeSelected = false;
    };

    DatetimeTypeFlow.prototype.ok = function ok() {
      if (this.component.show === 'date') {
        this.component.showTimePicker();
      } else {
        this.component.close();
      }
    };

    DatetimeTypeFlow.prototype.selectDay = function selectDay(year, month, day) {
      TypeFlow$$1.prototype.selectDay.call(this, year, month, day);

      if (this.component.autoContinue) {
        this.component.showTimePicker();
      }
    };

    return DatetimeTypeFlow;
  }(TypeFlow));

  var TimeTypeFlow = (function (TypeFlow$$1) {
    function TimeTypeFlow() {
      TypeFlow$$1.apply(this, arguments);
    }

    if (TypeFlow$$1) TimeTypeFlow.__proto__ = TypeFlow$$1;
    TimeTypeFlow.prototype = Object.create(TypeFlow$$1 && TypeFlow$$1.prototype);
    TimeTypeFlow.prototype.constructor = TimeTypeFlow;

    TimeTypeFlow.prototype.inputFormat = function inputFormat() {
      return 'HH:mm'
    };

    TimeTypeFlow.prototype.open = function open() {
      this.component.showTimePicker();
    };

    TimeTypeFlow.prototype.isoDate = function isoDate() {
      return this.date.format('HH:mm') + ':00Z'
    };

    return TimeTypeFlow;
  }(TypeFlow));

  var typeFlowFactory = function (type, component, date) {
    var typeFlow;

    switch (type) {
      case 'datetime':
        typeFlow = new DatetimeTypeFlow(component, date);
        break
      case 'time':
        typeFlow = new TimeTypeFlow(component, date);
        break
      case 'date':
      default:
        typeFlow = new DateTypeFlow(component, date);
        break
    }

    return typeFlow
  };

  var Datetime = {
    render: function () { var _vm = this; var _h = _vm.$createElement; var _c = _vm._self._c || _h; return _c('div', { staticClass: "vdatetime", class: _vm.wrapperClass }, [_c('input', _vm._g(_vm._b({ ref: "input", class: _vm.inputClass, attrs: { "type": "text", "readonly": "readonly", "placeholder": _vm.placeholder, "required": _vm.required }, domProps: { "value": _vm.inputValue }, on: { "click": _vm.open, "focus": _vm.open } }, 'input', _vm.$attrs, false), _vm.$listeners)), _vm._v(" "), _c('transition', { attrs: { "name": "vdatetime-fade" } }, [(_vm.isOpen) ? _c('div', [_c('div', { staticClass: "vdatetime-overlay", on: { "click": function ($event) { if ($event.target !== $event.currentTarget) { return null; } _vm.close(false); } } }), _vm._v(" "), _c('div', { staticClass: "vdatetime-popup" }, [_c('div', { staticClass: "vdatetime-popup__header" }, [_c('div', { staticClass: "vdatetime-popup__year", on: { "click": _vm.showYearPicker } }, [_vm._v(_vm._s(_vm.newYear))]), _vm._v(" " + _vm._s(_vm.newDay) + " ")]), _vm._v(" "), _c('div', { ref: "popupBody", staticClass: "vdatetime-popup__body" }, [_c('div', { directives: [{ name: "show", rawName: "v-show", value: (_vm.show === 'date'), expression: "show === 'date'" }] }, [_c('div', { staticClass: "vdatetime-popup__month-selector" }, [_c('div', { staticClass: "vdatetime-popup__month-selector__previous", on: { "click": _vm.previousMonth } }, [_c('svg', { attrs: { "xmlns": "http://www.w3.org/2000/svg", "viewBox": "0 0 61.3 102.8" } }, [_c('path', { attrs: { "fill": "none", "stroke": "#444", "stroke-width": "14", "stroke-miterlimit": "10", "d": "M56.3 97.8L9.9 51.4 56.3 5" } })])]), _vm._v(" "), _c('div', { staticClass: "vdatetime-popup__month-selector__current" }, [_vm._v(_vm._s(_vm.currentMonth))]), _vm._v(" "), _c('div', { staticClass: "vdatetime-popup__month-selector__next", on: { "click": _vm.nextMonth } }, [_c('svg', { attrs: { "xmlns": "http://www.w3.org/2000/svg", "viewBox": "0 0 61.3 102.8" } }, [_c('path', { attrs: { "fill": "none", "stroke": "#444", "stroke-width": "14", "stroke-miterlimit": "10", "d": "M56.3 97.8L9.9 51.4 56.3 5" } })])])]), _vm._v(" "), _c('div', { staticClass: "vdatetime-popup__date-picker", style: ({ height: _vm.datePickerHeight }) }, [_vm._l((_vm.weekdays), function (weekday) { return _c('div', { staticClass: "vdatetime-popup__date-picker__item vdatetime-popup__date-picker__item--header" }, [_vm._v(_vm._s(weekday))]) }), _vm._v(" "), _vm._l((_vm.currentMonthDays), function (day) { return _c('div', { staticClass: "vdatetime-popup__date-picker__item", class: { 'vdatetime-popup__date-picker__item--selected': day.selected, 'vdatetime-popup__date-picker__item--disabled': day.disabled }, on: { "click": function ($event) { !day.disabled && _vm.selectDay(day.number); } } }, [_c('span', [_c('span', [_vm._v(_vm._s(day.number))])])]) })], 2)]), _vm._v(" "), _c('div', { directives: [{ name: "show", rawName: "v-show", value: (_vm.show === 'time'), expression: "show === 'time'" }], staticClass: "vdatetime-popup__list-picker-wrapper" }, [_c('div', { ref: "hourPicker", staticClass: "vdatetime-popup__list-picker vdatetime-popup__list-picker--half" }, _vm._l((_vm.hours), function (hour) { return _c('div', { staticClass: "vdatetime-popup__list-picker__item", class: { 'vdatetime-popup__list-picker__item--selected': hour.selected }, on: { "click": function ($event) { _vm.selectHour(hour.number); } } }, [_vm._v(_vm._s(hour.number))]) })), _vm._v(" "), _c('div', { ref: "minutePicker", staticClass: "vdatetime-popup__list-picker vdatetime-popup__list-picker--half" }, _vm._l((_vm.minutes), function (minute) { return _c('div', { staticClass: "vdatetime-popup__list-picker__item", class: { 'vdatetime-popup__list-picker__item--selected': minute.selected }, on: { "click": function ($event) { _vm.selectMinute(minute.number); } } }, [_vm._v(_vm._s(minute.number))]) }))]), _vm._v(" "), _c('div', { directives: [{ name: "show", rawName: "v-show", value: (_vm.show === 'year'), expression: "show === 'year'" }], staticClass: "vdatetime-popup__list-picker-wrapper" }, [_c('div', { ref: "yearPicker", staticClass: "vdatetime-popup__list-picker" }, _vm._l((_vm.years), function (year) { return _c('div', { staticClass: "vdatetime-popup__list-picker__item", class: { 'vdatetime-popup__list-picker__item--selected': year.selected }, on: { "click": function ($event) { _vm.selectYear(year.number); } } }, [_vm._v(_vm._s(year.number))]) }))])]), _vm._v(" "), _c('div', { staticClass: "vdatetime-popup__actions" }, [_c('div', { staticClass: "vdatetime-popup__actions__button", on: { "click": function ($event) { _vm.close(false); } } }, [_vm._v("Cancel")]), _vm._v(" "), _c('div', { staticClass: "vdatetime-popup__actions__button", on: { "click": _vm.ok } }, [_vm._v("Ok")])])])]) : _vm._e()])], 1) }, staticRenderFns: [],
    props: {
      value: {
        type: String,
        required: true
      },
      type: {
        type: String,
        default: 'date'
      },
      inputFormat: {
        type: String,
        default: ''
      },
      wrapperClass: {
        type: String
      },
      inputClass: {
        type: String
      },
      placeholder: {
        type: String
      },
      locale: {
        type: String,
        default: null
      },
      minDate: {
        type: String,
        default: null
      },
      maxDate: {
        type: String,
        default: null
      },
      disabledDates: {
        type: Array,
        default: function default$1() {
          return []
        }
      },
      mondayFirst: {
        type: Boolean,
        default: false,
      },
      autoContinue: {
        type: Boolean,
        default: false
      },
      autoClose: {
        type: Boolean,
        default: false
      },
      required: {
        type: Boolean,
        default: false
      }
    },

    data: function data() {
      var date = this.getDate();

      return {
        isOpen: false,
        show: null,
        date: date,
        newDate: null,
        currentMonthDate: null,
        typeFlow: typeFlowFactory(this.type, this, date ? date.clone() : moment().locale(this.locale)),
        datePickerItemHeight: null
      }
    },

    watch: {
      value: function value(newValue) {
        this.date = this.getDate();
        this.typeFlow.setDate(this.date ? this.date.clone() : moment().locale(this.locale));
        this.newDate = this.getNewDate();
        this.currentMonthDate = this.getCurrentMonthDate();
      }
    },

    created: function created() {
      if (this.date) {
        this.$emit('input', this.typeFlow.isoDate());
      }
    },

    computed: {
      inputValue: function inputValue() {
        return this.typeFlow.format(this.date, this.inputFormat || this.typeFlow.inputFormat())
      },
      newDay: function newDay() {
        return this.newDate.format('ddd, MMM D')
      },
      newYear: function newYear() {
        return this.newDate.format('YYYY')
      },
      currentMonth: function currentMonth() {
        return this.currentMonthDate.format('MMMM YYYY')
      },
      weekdays: function weekdays$$1() {
        return weekdays(this.locale, this.mondayFirst)
      },
      currentMonthDays: function currentMonthDays() {
        var this$1 = this;

        var currentYear = this.currentMonthDate.year();
        var currentMonth = this.currentMonthDate.month();
        var isCurrentMonth = currentYear === this.newDate.year() &&
          currentMonth === this.newDate.month();

        var days = monthDays(currentMonth, currentYear, this.mondayFirst);

        return days.map(function (day) {
          return {
            number: day || '',
            selected: day ? isCurrentMonth && day === this$1.newDate.date() : false,
            disabled: day ? this$1.isDisabled(moment([currentYear, currentMonth, day])) : true
          }
        })
      },
      years: function years$$1() {
        var this$1 = this;

        return years().map(function (year) {
          return {
            number: year,
            selected: year === this$1.newDate.year()
          }
        })
      },
      hours: function hours$$1() {
        var this$1 = this;

        return hours().map(function (hour) {
          return {
            number: hour,
            selected: parseInt(hour) === parseInt(this$1.newDate.hour())
          }
        })
      },
      minutes: function minutes$$1() {
        var this$1 = this;

        return minutes().map(function (minute) {
          return {
            number: minute,
            selected: parseInt(minute) === this$1.newDate.minute()
          }
        })
      },
      disabledDatesRanges: function disabledDatesRanges() {
        return this.disabledDates.map(function (item) {
          return Array.isArray(item) ? [moment(item[0]), moment(item[1])] : [moment(item), moment(item).add(1, 'day')]
        })
      },
      datePickerHeight: function datePickerHeight() {
        var height = (Math.ceil(this.currentMonthDays.length / 7) + 1) * this.datePickerItemHeight;

        return height ? height + 'px' : 'auto'
      }
    },

    methods: {
      getDate: function getDate() {
        return this.value.length ? moment(this.value, this.type === 'time' ? 'HH:mm' : null).locale(this.locale) : null
      },
      getNewDate: function getNewDate() {
        var newDate = this.date ? this.date.clone() : moment().locale(this.locale);

        for (var i = 0; i < 1e5 && this.isDisabled(newDate); i++) {
          newDate = newDate.clone().add(1, 'day');
        }

        return newDate
      },
      getCurrentMonthDate: function getCurrentMonthDate() {
        return moment([this.newDate.year(), this.newDate.month(), 1]).locale(this.locale)
      },
      open: function open() {
        var this$1 = this;

        this.newDate = this.getNewDate();
        this.currentMonthDate = this.getCurrentMonthDate();

        this.isOpen = true;
        this.$refs.input.blur();
        this.typeFlow.open();

        this.$nextTick(function () {
          var height = (this$1.$refs.popupBody.clientHeight - 25) + 'px';
          this$1.$refs.hourPicker.style.height = height;
          this$1.$refs.minutePicker.style.height = height;
          this$1.$refs.yearPicker.style.height = height;
        });
      },
      close: function close(save) {
        if (save === void 0) save = true;

        this.typeFlow.close();

        if (save === true) {
          this.date = this.typeFlow.date.clone();
          this.$emit('input', this.typeFlow.isoDate());
        }

        this.isOpen = false;
      },
      ok: function ok() {
        if (this.show === 'year') {
          this.showDatePicker();
        } else {
          this.typeFlow.ok();
        }
      },
      showDatePicker: function showDatePicker() {
        var this$1 = this;

        this.show = 'date';

        this.$nextTick(function () {
          this$1.datePickerItemHeight = this$1.$refs.popupBody.getElementsByClassName('vdatetime-popup__date-picker__item')[7].offsetHeight;
        });
      },
      showTimePicker: function showTimePicker() {
        var this$1 = this;

        this.show = 'time';

        this.$nextTick(function () {
          var selectedHour = this$1.$refs.hourPicker.querySelector('.vdatetime-popup__list-picker__item--selected');
          var selectedMinute = this$1.$refs.minutePicker.querySelector('.vdatetime-popup__list-picker__item--selected');

          this$1.$refs.hourPicker.scrollTop = selectedHour ? selectedHour.offsetTop - 250 : 0;
          this$1.$refs.minutePicker.scrollTop = selectedMinute ? selectedMinute.offsetTop - 250 : 0;
        });
      },
      showYearPicker: function showYearPicker() {
        var this$1 = this;

        this.show = 'year';

        this.$nextTick(function () {
          var selectedYear = this$1.$refs.yearPicker.querySelector('.vdatetime-popup__list-picker__item--selected');

          this$1.$refs.yearPicker.scrollTop = selectedYear ? selectedYear.offsetTop - 250 : 0;
        });
      },
      previousMonth: function previousMonth() {
        this.currentMonthDate = this.currentMonthDate.clone().subtract(1, 'month');
      },
      nextMonth: function nextMonth() {
        this.currentMonthDate = this.currentMonthDate.clone().add(1, 'month');
      },
      selectYear: function selectYear(year) {
        this.currentMonthDate = this.currentMonthDate.clone().year(year);
        this.newDate = this.newDate.clone().year(year);

        if (this.autoContinue) {
          this.showDatePicker();
        }
      },
      selectDay: function selectDay(day) {
        this.typeFlow.selectDay(this.currentMonthDate.year(), this.currentMonthDate.month(), day);
        this.newDate = this.typeFlow.date.clone();
      },
      selectHour: function selectHour(hour) {
        this.typeFlow.selectHour(hour);
        this.newDate = this.typeFlow.date.clone();
      },
      selectMinute: function selectMinute(minute) {
        this.typeFlow.selectMinute(minute);
        this.newDate = this.typeFlow.date.clone();
      },
      isDisabled: function isDisabled(date) {
        return (this.minDate && date.isBefore(this.minDate, 'day')) ||
          (this.maxDate && date.isAfter(this.maxDate, 'day')) ||
          (this.disabledDatesRanges && this.disabledDatesRanges.find(function (dates) {
            return date.isBetween(dates[0], dates[1], 'day', '[)')
          }) !== undefined)
      }
    }
  };

  return Datetime;

}());
