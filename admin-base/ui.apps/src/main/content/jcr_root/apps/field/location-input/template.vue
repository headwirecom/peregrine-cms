<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2026 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
    <input
        type="text"
        :value="value"
        :readonly="schema.readonly"
        @input="updateValue($event.target.value)"
    />
</template>

<script>
export default {
    name: 'FieldLocationInput',
    mixins: [VueFormGenerator.abstractField],
    methods: {
        updateValue(value) {
            this.value = value;
        }
    },
    mounted() {
        const vm = this;
        const input = this.$el;
        if (!input || input._autocompleteAttached) return;

        input._autocompleteAttached = true;

        const listboxId = `location-autocomplete-listbox-${crypto.randomUUID()}`;
        input.setAttribute('role', 'combobox');
        input.setAttribute('aria-autocomplete', 'list');
        input.setAttribute('aria-expanded', 'false');
        input.setAttribute('aria-controls', listboxId);
        input.setAttribute('aria-haspopup', 'listbox');

        const dropdown = document.createElement('ul');
        dropdown.id = listboxId;
        dropdown.setAttribute('role', 'listbox');
        dropdown.classList.add('location-input-dropdown');
        input.parentElement.classList.add('location-input-wrapper');
        input.insertAdjacentElement('afterend', dropdown);

        let activeIndex = -1;
        let featureList = [];

        const setActiveItem = (index) => {
            const items = dropdown.querySelectorAll('[role="option"]');
            items.forEach((item, i) => {
                const isActive = i === index;
                item.setAttribute('aria-selected', String(isActive));
                item.classList.toggle('is-active', isActive);
            });
            activeIndex = index;
            if (index >= 0 && items[index]) {
                input.setAttribute('aria-activedescendant', items[index].id);
                items[index].scrollIntoView({ block: 'nearest' });
            } else {
                input.removeAttribute('aria-activedescendant');
            }
        };

        const hideDropdown = () => {
            dropdown.style.display = 'none';
            dropdown.innerHTML = '';
            activeIndex = -1;
            input.setAttribute('aria-expanded', 'false');
            input.removeAttribute('aria-activedescendant');
        };

        const formatAddress = ({ street, name, housenumber, postcode, city }) => {
            const streetPart = [street || name, housenumber].filter(Boolean).join(' ');
            const cityPart = [postcode, city].filter(Boolean).join(' ');
            return [streetPart, cityPart].filter(Boolean).join(', ');
        };

        const selectFeature = (feature) => {
            vm.value = formatAddress(feature.properties);
            hideDropdown();
            input.focus();
        };

        const showSuggestions = (features) => {
            featureList = features.filter((f) => f.properties.street || f.properties.name);
            dropdown.innerHTML = '';
            if (!featureList.length) { hideDropdown(); return; }
            featureList.forEach((feature, i) => {
                const label = formatAddress(feature.properties);
                const li = document.createElement('li');
                li.textContent = label;
                li.id = `location-option-${i}`;
                li.setAttribute('role', 'option');
                li.setAttribute('aria-selected', 'false');
                li.addEventListener('mouseenter', () => setActiveItem(i));
                li.addEventListener('mouseleave', () => { activeIndex = i; });
                li.addEventListener('mousedown', (e) => { e.preventDefault(); selectFeature(feature); });
                dropdown.append(li);
            });
            dropdown.style.display = 'block';
            dropdown.style.width = input.offsetWidth + 'px';
            dropdown.scrollTop = 0;
            input.setAttribute('aria-expanded', 'true');
            setActiveItem(0);
        };

        input.addEventListener('keydown', (e) => {
            if (dropdown.style.display === 'none') return;
            const items = dropdown.querySelectorAll('[role="option"]');
            if (e.key === 'ArrowDown') { e.preventDefault(); setActiveItem(Math.min(activeIndex + 1, items.length - 1)); }
            else if (e.key === 'ArrowUp') { e.preventDefault(); setActiveItem(Math.max(activeIndex - 1, -1)); }
            else if (e.key === 'Enter' && featureList.length > 0) { e.preventDefault(); selectFeature(featureList[activeIndex >= 0 ? activeIndex : 0]); }
            else if (e.key === 'Escape') { hideDropdown(); }
        });

        let debounceTimer;
        input.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            const val = input.value.trim();
            if (val.length < 3) { hideDropdown(); return; }
            debounceTimer = setTimeout(async () => {
                try {
                    const res = await fetch(`https://photon.komoot.io/api/?q=${encodeURIComponent(val)}&limit=50&lang=en&bbox=5.9,45.8,10.5,47.8`);
                    if (!res.ok) return;
                    const data = await res.json();
                    showSuggestions(data.features || []);
                } catch { /* Do nothing */ }
            }, 300);
        });

        input.addEventListener('blur', hideDropdown);

        const onScroll = (e) => {
            if (dropdown.style.display !== 'none' && !dropdown.contains(e.target)) hideDropdown();
        };
        window.addEventListener('scroll', onScroll, { capture: true, passive: true });

        this.$once('hook:beforeDestroy', () => {
            window.removeEventListener('scroll', onScroll, { capture: true });
        });
    }
}
</script>

<style>
.location-input-wrapper {
    position: relative;
}

.location-input-dropdown {
    background: white;
    border: 1px solid var(--pcms-blue-grey-darken-3);
    border-radius: 4px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
    display: none;
    list-style: none !important;
    margin: 0;
    max-height: 200px;
    overflow-y: auto;
    padding: 0;
    position: absolute;
    top: 100%;
    left: 0;
    z-index: 9999;
}

.location-input-dropdown > [role='option'] {
    border-bottom: 1px solid #cfd8dc;
    cursor: pointer;
    padding: 8px 12px;
    white-space: normal;
    word-break: break-word;
}

.location-input-dropdown > [role='option']:last-child {
    border-bottom: none;
}

.location-input-dropdown > [role='option'].is-active {
    background: #eceff1;
}
</style>
